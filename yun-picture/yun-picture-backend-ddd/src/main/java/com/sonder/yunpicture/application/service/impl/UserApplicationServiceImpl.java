package com.sonder.yunpicture.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sonder.yunpicture.application.service.UserApplicationService;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicture.domain.user.service.UserDomainService;
import com.sonder.yunpicture.infrastructure.common.DeleteRequest;
import com.sonder.yunpicture.infrastructure.exception.BusinessException;
import com.sonder.yunpicture.infrastructure.exception.ErrorCode;
import com.sonder.yunpicture.infrastructure.exception.ThrowUtils;
import com.sonder.yunpicture.interfaces.dto.user.UserLoginRequest;
import com.sonder.yunpicture.interfaces.dto.user.UserQueryRequest;
import com.sonder.yunpicture.interfaces.dto.user.UserRegisterRequest;
import com.sonder.yunpicture.interfaces.vo.user.LoginUserVO;
import com.sonder.yunpicture.interfaces.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author Sonder
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-31 20:46:47
*/
@Service
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService userDomainService;

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        // 校验
        User.validUserRegister(userRegisterRequest);
        // 执行注册
        return userDomainService.userRegister(userRegisterRequest);
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 校验
        User.validUserLogin(userAccount, userPassword);
        // 执行登录
        return userDomainService.userLogin(userAccount, userPassword, request);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        return userDomainService.getLoginUserVO(user);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userDomainService.getUserVO(user);
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        return userDomainService.getUserVOList(userList);
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        return userDomainService.getEncryptPassword(userPassword);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return userDomainService.getLoginUser(request);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        return userDomainService.userLogout(request);
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        return userDomainService.getQueryWrapper(userQueryRequest);
    }

    @Override
    public User getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userDomainService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return user;
    }

    @Override
    public UserVO getUserVOById(long id) {
        return userDomainService.getUserVO(getUserById(id));
    }

    @Override
    public boolean deleteUser(DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userDomainService.removeById(deleteRequest.getId());
    }

    @Override
    public void updateUser(User user) {
        boolean result = userDomainService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userDomainService.page(new Page<>(current, size),
                userDomainService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userDomainService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return userVOPage;
    }

    @Override
    public List<User> listByIds(Set<Long> userIdSet) {
        return userDomainService.listByIds(userIdSet);
    }

    @Override
    public long saveUser(User userEntity) {
        // 填充字段
        final String DEFAULT_PASSWORD = "12345678";
        userEntity.setUserPassword(userDomainService.getEncryptPassword(DEFAULT_PASSWORD));
        //插入数据库
        boolean result = userDomainService.saveUser(userEntity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return userEntity.getId();
    }


}




