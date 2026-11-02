package com.javaee.documentservice.controller;

import com.javaee.common.utils.JwtUtils;
import com.javaee.documentservice.collaborate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * 协同编辑控制器，通过 STOMP 消息处理加入、离开、编辑、光标与增量同步。
 *
 * <p>用户身份取自 WebSocket 握手的 {@link Principal}，无需客户端上报，
 * 每条消息处理完成后广播到 {@code /topic/doc/{documentId}}。</p>
 */
@Controller
public class CollaborateController {

    private static final Logger log = LoggerFactory.getLogger(CollaborateController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CollaborateSessionManager sessionManager;

    @Autowired
    private EditOperationHandler editOperationHandler;

    @Autowired
    private CursorSyncHandler cursorSyncHandler;

    @Autowired
    private DocumentSnapshotService snapshotService;

    /**
     * 处理用户加入文档，广播成员变更并回推其他协作者的光标。
     *
     * @param joinMessage 加入消息，只需携带文档 ID
     * @param principal 当前 WebSocket 会话的登录身份
     */
    @MessageMapping("/collaborate/join")
    public void joinDocument(@Payload DocumentJoinMessage joinMessage, Principal principal) {
        String userId = extractUserId(principal);
        String userName = extractUserName(principal);

        joinMessage.setUserId(userId);
        joinMessage.setUserName(userName);

        sessionManager.userJoinDocument(joinMessage.getDocumentId(), userId, userName);

        CollaborateMessage joinMsg = CollaborateMessage.join(joinMessage);
        messagingTemplate.convertAndSend("/topic/doc/" + joinMessage.getDocumentId(), joinMsg);

        List<CursorPosition> cursors = cursorSyncHandler.getDocumentCursors(joinMessage.getDocumentId());
        for (CursorPosition cursor : cursors) {
            if (!cursor.getUserId().equals(userId)) {
                messagingTemplate.convertAndSend("/topic/doc/" + joinMessage.getDocumentId(),
                        CollaborateMessage.cursor(cursor));
            }
        }

        log.info("用户加入协同编辑: userId={}, userName={}, documentId={}",
                userId, userName, joinMessage.getDocumentId());
    }

    /**
     * 处理用户离开文档，清理会话与光标并广播离开事件。
     *
     * @param leaveMessage 离开消息，只需携带文档 ID
     * @param principal 当前 WebSocket 会话的登录身份
     */
    @MessageMapping("/collaborate/leave")
    public void leaveDocument(@Payload DocumentJoinMessage leaveMessage, Principal principal) {
        String userId = extractUserId(principal);
        String userName = extractUserName(principal);

        leaveMessage.setUserId(userId);
        leaveMessage.setUserName(userName);

        sessionManager.userLeaveDocument(leaveMessage.getDocumentId(), userId);
        cursorSyncHandler.removeCursor(leaveMessage.getDocumentId(), userId);

        CollaborateMessage leaveMsg = CollaborateMessage.leave(leaveMessage);
        messagingTemplate.convertAndSend("/topic/doc/" + leaveMessage.getDocumentId(), leaveMsg);

        log.info("用户离开协同编辑: userId={}, documentId={}", userId, leaveMessage.getDocumentId());
    }

    /**
     * 处理编辑操作，分配逻辑时钟、写入操作缓存并广播给协作者。
     *
     * @param operation 客户端提交的编辑操作，服务端会覆盖用户与时钟字段
     * @param principal 当前 WebSocket 会话的登录身份
     */
    @MessageMapping("/collaborate/edit")
    public void handleEdit(@Payload EditOperation operation, Principal principal) {
        String userId = extractUserId(principal);
        String userName = extractUserName(principal);

        operation.setUserId(userId);
        operation.setUserName(userName);

        long clock = editOperationHandler.getNextClock(operation.getDocumentId(), userId);
        operation.setClock(clock);
        operation.setOpId(userId + "-" + clock);

        editOperationHandler.saveOperation(operation);

        CollaborateMessage editMsg = CollaborateMessage.edit(operation);
        messagingTemplate.convertAndSend("/topic/doc/" + operation.getDocumentId(), editMsg);

        log.debug("编辑操作已广播: opId={}, type={}, documentId={}, userId={}",
                operation.getOpId(), operation.getType(), operation.getDocumentId(), userId);
    }

    /**
     * 处理光标上报，补齐用户与时间戳后缓存并广播。
     *
     * @param cursor 客户端上报的光标位置
     * @param principal 当前 WebSocket 会话的登录身份
     */
    @MessageMapping("/collaborate/cursor")
    public void handleCursor(@Payload CursorPosition cursor, Principal principal) {
        String userId = extractUserId(principal);
        String userName = extractUserName(principal);

        cursor.setUserId(userId);
        cursor.setUserName(userName);
        cursor.setTimestamp(System.currentTimeMillis());

        cursorSyncHandler.updateCursor(cursor);

        CollaborateMessage cursorMsg = CollaborateMessage.cursor(cursor);
        messagingTemplate.convertAndSend("/topic/doc/" + cursor.getDocumentId(), cursorMsg);
    }

    /**
     * 处理增量同步请求，把客户端缺失的操作逐条单播回去。
     *
     * @param syncRequest 同步请求，包含 documentId 与 sinceClock
     * @param principal 当前 WebSocket 会话的登录身份
     */
    @MessageMapping("/collaborate/sync")
    public void handleSync(@Payload Map<String, Object> syncRequest, Principal principal) {
        String userId = extractUserId(principal);
        String documentId = (String) syncRequest.get("documentId");
        long sinceClock = syncRequest.get("sinceClock") != null
                ? ((Number) syncRequest.get("sinceClock")).longValue() : 0;

        List<EditOperation> missedOps = editOperationHandler.getOperationsSince(documentId, sinceClock);
        for (EditOperation op : missedOps) {
            CollaborateMessage editMsg = CollaborateMessage.edit(op);
            messagingTemplate.convertAndSendToUser(userId, "/topic/doc/" + documentId, editMsg);
        }

        log.debug("同步操作: userId={}, documentId={}, sinceClock={}, missedOps={}",
                userId, documentId, sinceClock, missedOps.size());
    }

    private String extractUserId(Principal principal) {
        if (principal != null) {
            return principal.getName();
        }
        return "anonymous";
    }

    private String extractUserName(Principal principal) {
        if (principal != null) {
            return principal.getName();
        }
        return "anonymous";
    }
}