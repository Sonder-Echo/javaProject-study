package com.sonder.yunpicture.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonder.yunpicture.domain.picture.entity.Picture;
import com.sonder.yunpicture.domain.picture.repository.PictureRepository;
import com.sonder.yunpicture.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
 * 图片仓储实现
 */
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
