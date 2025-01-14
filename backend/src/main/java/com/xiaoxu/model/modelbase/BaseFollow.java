package com.xiaoxu.model.modelbase;

import com.jfinal.plugin.activerecord.IBean;
import io.jboot.db.model.JbootModel;

/**
 * Generated by Jboot, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseFollow<M extends BaseFollow<M>> extends JbootModel<M> implements IBean {

    /**
     * id
     */
    public void setId(java.lang.Long id) {
        set("id", id);
    }

    /**
     * id
     */
    public java.lang.Long getId() {
        return getLong("id");
    }

    /**
     * 被关注者 id
     */
    public void setFolloweeId(java.lang.Long followeeId) {
        set("followeeId", followeeId);
    }

    /**
     * 被关注者 id
     */
    public java.lang.Long getFolloweeId() {
        return getLong("followeeId");
    }

    /**
     * 粉丝 id
     */
    public void setFollowerId(java.lang.Long followerId) {
        set("followerId", followerId);
    }

    /**
     * 粉丝 id
     */
    public java.lang.Long getFollowerId() {
        return getLong("followerId");
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

    /**
     * 更新时间
     */
    public void setUpdateTime(java.util.Date updateTime) {
        set("updateTime", updateTime);
    }

    /**
     * 更新时间
     */
    public java.util.Date getUpdateTime() {
        return getDate("updateTime");
    }

    /**
     * 是否删除
     */
    public void setIsDelete(java.lang.Integer isDelete) {
        set("isDelete", isDelete);
    }

    /**
     * 是否删除
     */
    public java.lang.Integer getIsDelete() {
        return getInt("isDelete");
    }

}

