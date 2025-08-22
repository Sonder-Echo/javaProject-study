package com.sonder.yunpicture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicture.domain.space.entity.SpaceUser;
import com.sonder.yunpicture.interfaces.dto.spaceuser.SpaceUserQueryRequest;

/**
 * @author Sonder
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-08-20 20:43:59
 */
public interface SpaceUserDomainService extends IService<SpaceUser> {

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
