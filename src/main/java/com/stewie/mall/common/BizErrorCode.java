package com.stewie.mall.common;

import lombok.Getter;

/**
 * 业务错误码枚举。集中管理错误码和文案,避免散落魔法数字。
 */
@Getter
public enum BizErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    BizErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
