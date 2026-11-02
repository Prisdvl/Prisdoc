package com.javaee.documentservice.collaborate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档加入事件消息，标识某用户以何种动作进入协同会话。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentJoinMessage {

    /** 文档 ID */
    private String documentId;

    /** 用户 ID */
    private String userId;

    /** 用户名，用于其他协作者的光标与成员展示 */
    private String userName;

    /** 动作标识，由客户端填写，服务端仅透传 */
    private String action;
}