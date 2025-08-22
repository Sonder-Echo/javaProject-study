package com.sonder.yunpicture.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sonder.yunpicture.domain.space.entity.Space;
import com.sonder.yunpicture.domain.user.entity.User;
import com.sonder.yunpicture.interfaces.dto.space.SpaceQueryRequest;

/**
 * @author Sonder
 * @description 针对表【space(图片)】的数据库操作Service
 * @createDate 2025-08-15 21:13:10
 */
public interface SpaceDomainService {

    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);

}
