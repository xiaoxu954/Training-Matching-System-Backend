package com.xiaoxu.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户分页请求
 */
@Data
public class UserPageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private Integer current;
    private Integer pageSize;

    private static final long serialVersionUID = 1L;
}