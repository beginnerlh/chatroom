package com.lh.vo;

public class MessageVO {

    /**
     * 告知服务端要进行的操作,eg:1表示新用户注册,2表示私聊,3表示群聊等
     */
    private String type;
    /**
     * 服务端与客户端聊天具体内容
     */
    private String content;
    /**
     * 聊天信息发送的目标客户端名称
     */
    private String to;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "MessageVO{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
