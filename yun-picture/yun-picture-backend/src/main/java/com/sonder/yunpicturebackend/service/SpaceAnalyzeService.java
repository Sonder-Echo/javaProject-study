package com.sonder.yunpicturebackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicturebackend.model.dto.space.analyze.*;
import com.sonder.yunpicturebackend.model.entity.Space;
import com.sonder.yunpicturebackend.model.entity.User;
import com.sonder.yunpicturebackend.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.sonder.yunpicturebackend.model.vo.space.analyze.SpaceSizeAnalyzeResponse;
import com.sonder.yunpicturebackend.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.sonder.yunpicturebackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;

import java.util.List;


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

    /**
     * 获取空间图片分类分析
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片标签分析
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);


}
