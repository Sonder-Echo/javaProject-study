package com.sonder.yunpicturebackend.service;

import com.sonder.yunpicturebackend.model.dto.UserRegisterRequest;
import com.sonder.yunpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicturebackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Sonder
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-31 20:46:47
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest  用户注册信息
     * @return  用户 id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request   http请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * @param user  用户实体类
     * @return  脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取加密密码
     * @param userPassword  用户输入的密码
     * @return  加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     * @param request   http请求
     * @return  当前登录用户
     */
    User getLoginUser(HttpServletRequest request);
}
