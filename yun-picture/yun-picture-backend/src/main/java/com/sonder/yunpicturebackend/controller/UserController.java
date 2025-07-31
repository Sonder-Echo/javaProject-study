package com.sonder.yunpicturebackend.controller;

import com.sonder.yunpicturebackend.common.BaseResponse;
import com.sonder.yunpicturebackend.common.ResultUtils;
import com.sonder.yunpicturebackend.exception.ErrorCode;
import com.sonder.yunpicturebackend.exception.ThrowUtils;
import com.sonder.yunpicturebackend.model.dto.UserRegisterRequest;
import com.sonder.yunpicturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册信息
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }
}
