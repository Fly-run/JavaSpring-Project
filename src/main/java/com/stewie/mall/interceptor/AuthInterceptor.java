package com.stewie.mall.interceptor;

import com.stewie.mall.util.JwtUtil;
import com.stewie.mall.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器。校验请求头 Authorization: Bearer <token>。
 * 校验通过则把用户 id 放入 ThreadLocal,不通过直接返回 401。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 放行预检请求(CORS 的 OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = JwtUtil.parse(token);
                UserContext.setUserId(Long.valueOf(claims.getSubject()));
                return true;
            } catch (Exception e) {
                // token 无效或过期,继续走下面的 401
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        // 请求结束务必清除 ThreadLocal,防止线程复用导致数据串号
        UserContext.clear();
    }
}
