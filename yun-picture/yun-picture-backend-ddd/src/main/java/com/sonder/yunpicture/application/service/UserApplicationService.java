package com.sonder.yunpicture.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sonder.yunpicture.infrastructure.common.DeleteRequest;
import com.sonder.yunpicture.interfaces.dto.user.UserLoginRequest;
import com.sonder.yunpicture.interfaces.dto.user.UserQueryRequest;
import com.sonder.yunpicture.interfaces.dto.user.UserRegisterRequest;
import com.sonder.yunpicture.domain.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicture.interfaces.vo.user.LoginUserVO;
import com.sonder.yunpicture.interfaces.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author Sonder
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-31 20:46:47
*/
public interface UserApplicationService{

    /**
     * 用户注册
     * @param userRegisterRequest  用户注册信息
     * @return  用户 id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * @param user  用户实体类
     * @return  脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     * @param user  用户实体类
     * @return  脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userList  用户实体类列表
     * @return  脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

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

    /**
     * 用户注销
     * @param request   http请求
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件（将java对象转化为Mybatis需要的QueryWrapper）
     * @param userQueryRequest  对象
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    User getUserById(long id);

    UserVO getUserVOById(long id);

    boolean deleteUser(DeleteRequest deleteRequest);

    void updateUser(User user);

    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    List<User> listByIds(Set<Long> userIdSet);

    long saveUser(User userEntity);
}
