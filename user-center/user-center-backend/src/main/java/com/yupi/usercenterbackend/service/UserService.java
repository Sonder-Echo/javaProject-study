package com.yupi.usercenterbackend.service;

import com.yupi.usercenterbackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author Sonder
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-07-11 20:38:30
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 密码校验
     * @param planetCode    星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      http请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param user 用户信息
     * @return
     */
    User getSafetyUser(User user);

    /**
     * 用户注销
     * @param request http请求
     * @return 0-注销成功
     */
    int userLogout(HttpServletRequest request);
}
