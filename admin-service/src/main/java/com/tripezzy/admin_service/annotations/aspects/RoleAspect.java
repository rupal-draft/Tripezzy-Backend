package com.tripezzy.admin_service.annotations.aspects;


import com.tripezzy.admin_service.annotations.RoleRequired;
import com.tripezzy.admin_service.auth.UserContext;
import com.tripezzy.admin_service.auth.UserContextHolder;
import com.tripezzy.admin_service.exceptions.AccessForbidden;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Aspect
@Component
public class RoleAspect {

    @Before("@annotation(com.tripezzy.admin_service.annotations.RoleRequired)")
    public void checkUserRole(org.aspectj.lang.JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RoleRequired roleRequired = method.getAnnotation(RoleRequired.class);
        String requiredRole = roleRequired.value();

        UserContext userContext = UserContextHolder.getUserDetails();
        if (userContext == null || !requiredRole.equals(userContext.getRole())) {
            throw new AccessForbidden("Access Denied: Insufficient Permissions");
        }

    }
}
