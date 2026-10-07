package com.stewie.mall.util;

/**
 * 当前登录用户上下文。用 ThreadLocal 存储,拦截器在请求进来时写入、结束时清除。
 * 这样业务代码里随时能拿到当前用户 id,而不用层层传参。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 必须在请求结束后调用,否则线程复用会串数据(ThreadLocal 内存泄漏) */
    public static void clear() {
        USER_ID.remove();
    }
}
