package com.sonder.yunpicturebackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.sonder.yunpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.sonder.yunpicturebackend.model.entity.Space;
import com.sonder.yunpicturebackend.model.entity.User;
import com.sonder.yunpicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;


/**
 * @author Sonder
 * @description 针对表【space(图片)】的数据库操作Service
 * @createDate 2025-08-15 21:13:10
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

}
