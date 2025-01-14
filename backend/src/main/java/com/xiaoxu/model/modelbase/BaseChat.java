package com.xiaoxu.model.modelbase;

import com.jfinal.plugin.activerecord.IBean;
import io.jboot.db.model.JbootModel;

/**
 * Generated by Jboot, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseChat<M extends BaseChat<M>> extends JbootModel<M> implements IBean {

    /**
     * 聊天记录id
     */
    public void setId(java.lang.Long id) {
        set("id", id);
    }

    /**
     * 聊天记录id
     */
    public java.lang.Long getId() {
        return getLong("id");
    }

    /**
     * 发送消息id
     */
    public void setFromId(java.lang.Long fromId) {
        set("fromId", fromId);
    }

    /**
     * 发送消息id
     */
    public java.lang.Long getFromId() {
        return getLong("fromId");
    }

    /**
     * 接收消息id
     */
    public void setToId(java.lang.Long toId) {
        set("toId", toId);
    }

    /**
     * 接收消息id
     */
    public java.lang.Long getToId() {
        return getLong("toId");
    }

    public void setText(java.lang.String text) {
        set("text", text);
    }

    public java.lang.String getText() {
        return getStr("text");
    }

    /**
     * 聊天类型 1-私聊 2-群聊
     */
    public void setChatType(java.lang.Integer chatType) {
        set("chatType", chatType);
    }

    /**
     * 聊天类型 1-私聊 2-群聊
     */
    public java.lang.Integer getChatType() {
        return getInt("chatType");
    }

    /**
     * 创建时间
     */
    public void setCreateTime(java.util.Date createTime) {
        set("createTime", createTime);
    }

    /**
     * 创建时间
     */
    public java.util.Date getCreateTime() {
        return getDate("createTime");
    }

    public void setUpdateTime(java.util.Date updateTime) {
        set("updateTime", updateTime);
    }

    public java.util.Date getUpdateTime() {
        return getDate("updateTime");
    }

    public void setTeamId(java.lang.Long teamId) {
        set("teamId", teamId);
    }

    public java.lang.Long getTeamId() {
        return getLong("teamId");
    }

    public void setIsDelete(java.lang.Integer isDelete) {
        set("isDelete", isDelete);
    }

    public java.lang.Integer getIsDelete() {
        return getInt("isDelete");
    }

}

