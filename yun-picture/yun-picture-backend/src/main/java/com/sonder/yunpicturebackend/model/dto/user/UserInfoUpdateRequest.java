package com.sonder.yunpicturebackend.model.dto.user;

import lombok.Data;

@Data
public class UserInfoUpdateRequest {

    /**
     * 用户名（展示）
     */
    private String userName;

    /**
     * 用户简介
     */
    private String userProfile;
}
