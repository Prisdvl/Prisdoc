package com.javaee.documentservice.collaborate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同编辑操作模型，描述一次插入或删除的区间、文本与逻辑时钟。
 *
 * <p>{@code opId} 由「用户 ID-逻辑时钟」拼成，用于幂等去重；{@code from}/{@code to}
 * 为相对文档正文的字符下标。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditOperation {

    /** 操作唯一标识，格式为「用户 ID-逻辑时钟」 */
    private String opId;

    /** 文档 ID */
    private String documentId;

    /** 发起操作的用户 ID */
    private String userId;

    /** 发起操作的用户名，用于前端展示 */
    private String userName;

    /** 操作类型 */
    private OpType type;

    /** 操作区间起点，插入时为插入位置 */
    private int from;

    /** 操作区间终点，插入时与起点相同 */
    private int to;

    /** 插入的文本，删除操作为空串 */
    private String text;

    /** 逻辑时钟，由服务端按「文档-用户」维度单调递增分配 */
    private long clock;

    /**
     * 编辑操作类型。
     */
    public enum OpType {
        // 在指定位置插入文本
        INSERT,
        // 删除指定区间内的文本
        DELETE
    }

    /**
     * 构造一次插入操作。
     *
     * @param documentId 文档 ID
     * @param userId 发起操作的用户 ID
     * @param userName 发起操作的用户名
     * @param position 插入位置，取文档正文的字符下标
     * @param text 插入的文本
     * @param clock 逻辑时钟
     * @return 类型为 INSERT 的编辑操作
     */
    public static EditOperation createInsert(String documentId, String userId, String userName,
                                              int position, String text, long clock) {
        EditOperation op = new EditOperation();
        op.setOpId(userId + "-" + clock);
        op.setDocumentId(documentId);
        op.setUserId(userId);
        op.setUserName(userName);
        op.setType(OpType.INSERT);
        op.setFrom(position);
        op.setTo(position);
        op.setText(text);
        op.setClock(clock);
        return op;
    }

    /**
     * 构造一次删除操作。
     *
     * @param documentId 文档 ID
     * @param userId 发起操作的用户 ID
     * @param userName 发起操作的用户名
     * @param from 删除区间起点，含
     * @param to 删除区间终点，不含
     * @param clock 逻辑时钟
     * @return 类型为 DELETE 的编辑操作
     */
    public static EditOperation createDelete(String documentId, String userId, String userName,
                                              int from, int to, long clock) {
        EditOperation op = new EditOperation();
        op.setOpId(userId + "-" + clock);
        op.setDocumentId(documentId);
        op.setUserId(userId);
        op.setUserName(userName);
        op.setType(OpType.DELETE);
        op.setFrom(from);
        op.setTo(to);
        op.setText("");
        op.setClock(clock);
        return op;
    }
}