package com.yupi.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.usercenterbackend.common.BaseResponse;
import com.yupi.usercenterbackend.common.ErrorCode;
import com.yupi.usercenterbackend.common.ResultUtils;
import com.yupi.usercenterbackend.constant.UserConstant;
import com.yupi.usercenterbackend.exception.BusinessException;
import com.yupi.usercenterbackend.model.domain.User;
import com.yupi.usercenterbackend.model.request.UserLoginRequest;
import com.yupi.usercenterbackend.model.request.UserRegisterRequest;
import com.yupi.usercenterbackend.service.UserService;
import com.yupi.usercenterbackend.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.usercenterbackend.constant.UserConstant.ADMIN_ROLE;
import static com.yupi.usercenterbackend.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        long result =  userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        User user =  userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        int result =  userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // TODO: 校验用户是否合法
        User user = userService.getById(currentUser.getId());
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        //鉴权--仅管理员可以查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> users = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        //鉴权--仅管理员可以进行删除操作
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不能小于0");
        }
        Boolean result =  userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        //鉴权--仅管理员可以查询
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        return currentUser != null && currentUser.getUserRole() == ADMIN_ROLE;
    }


}
