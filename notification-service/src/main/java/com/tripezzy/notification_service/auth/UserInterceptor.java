package com.tripezzy.notification_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if(userId != null && role != null) {
            UserContextHolder.setUserDetails(Long.parseLong(userId), role);
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        UserContextHolder.clearUserDetails();

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
