package com.sonder.yunpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonder.yunpicturebackend.model.entity.User;
import com.sonder.yunpicturebackend.service.UserService;
import com.sonder.yunpicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Sonder
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-31 20:46:47
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




