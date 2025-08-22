package com.sonder.yunpicture.infrastructure.aop;

import com.sonder.yunpicture.infrastructure.annotation.AuthCheck;
import com.sonder.yunpicture.infrastructure.exception.BusinessException;
import com.sonder.yunpicture.infrastructure.exception.ErrorCode;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicture.domain.user.valueobject.UserRoleEnum;
import com.sonder.yunpicture.application.service.UserApplicationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect // 切面
@Component
public class AuthInterceptor {

    @Resource
    private UserApplicationService userApplicationService;


    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userApplicationService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 如果不需要权限，放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        // 以下的代码，必须有权限，才会通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，若登录用户不是管理员，则无权限
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 放行
        return joinPoint.proceed();
    }
}
