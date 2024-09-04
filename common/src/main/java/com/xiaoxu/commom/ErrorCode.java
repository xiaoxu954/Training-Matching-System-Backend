package com.xiaoxu.commom;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    MESSAGE_NULL(40001, "请求参数为空"),
    ACCOUNT_TOO_SHIRT(40002, "用户账号过短"),
    PASSWORD_TOO_SHIRT(40003, "用户密码过短"),
    TWO_PASSWORD_NOT_SAME(40004, "两次输入的密码不一致"),
    ACCOUNT_EXIST(40005, "用户账号已存在"),
    ACCOUNT_NOT_EXIST(40006, "用户账号不存在"),
    PASSWORD_ERROR(40007, "用户密码错误"),
    ACCOUNT_NOT_ACTIVE(40008, "用户账号未激活"),
    ACCOUNT_ALREADY_ACTIVE(40009, "用户账号已激活"),

    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    TOO_MANY_REQUEST(42900, "请求过于频繁"),
    FORBIDDEN_ERROR(40300, "禁止访问"),

    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
