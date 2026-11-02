package com.javaee.documentservice.collaborate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同编辑消息载体，用 type 区分编辑、光标、加入与离开四类事件。
 *
 * <p>四类事件共用一个传输结构，仅对应字段有值，其余字段为 {@code null}；
 * 静态工厂方法负责按事件类型完成封装。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaborateMessage {

    /** 消息类型：EDIT-编辑，CURSOR-光标，JOIN-加入，LEAVE-离开 */
    private String type;

    /** 编辑操作，type 为 EDIT 时有值 */
    private EditOperation editOperation;

    /** 光标位置，type 为 CURSOR 时有值 */
    private CursorPosition cursorPosition;

    /** 加入或离开文档的用户信息，type 为 JOIN、LEAVE 时有值 */
    private DocumentJoinMessage joinMessage;

    /**
     * 封装一条编辑操作消息。
     *
     * @param op 待广播的编辑操作
     * @return 类型为 EDIT 的协同消息
     */
    public static CollaborateMessage edit(EditOperation op) {
        CollaborateMessage msg = new CollaborateMessage();
        msg.setType("EDIT");
        msg.setEditOperation(op);
        return msg;
    }

    public static CollaborateMessage cursor(CursorPosition cursor) {
        CollaborateMessage msg = new CollaborateMessage();
        msg.setType("CURSOR");
        msg.setCursorPosition(cursor);
        return msg;
    }

    public static CollaborateMessage join(DocumentJoinMessage join) {
        CollaborateMessage msg = new CollaborateMessage();
        msg.setType("JOIN");
        msg.setJoinMessage(join);
        return msg;
    }

    public static CollaborateMessage leave(DocumentJoinMessage leave) {
        CollaborateMessage msg = new CollaborateMessage();
        msg.setType("LEAVE");
        msg.setJoinMessage(leave);
        return msg;
    }
}