package com.sonder.yunpicture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicture.domain.user.repository.UserRepository;
import com.sonder.yunpicture.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户仓储实现
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
