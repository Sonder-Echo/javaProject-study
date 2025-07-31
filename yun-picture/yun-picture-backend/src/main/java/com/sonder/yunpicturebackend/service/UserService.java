package com.sonder.yunpicturebackend.service;

import com.sonder.yunpicturebackend.model.dto.UserRegisterRequest;
import com.sonder.yunpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 获取加密密码
     * @param userPassword
     * @param salt
     * @return
     */
    String getEncryptPassword(String userPassword);
}
