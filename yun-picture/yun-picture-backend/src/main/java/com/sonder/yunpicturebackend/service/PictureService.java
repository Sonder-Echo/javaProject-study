package com.sonder.yunpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sonder.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.sonder.yunpicturebackend.model.entity.Picture;
import com.sonder.yunpicturebackend.model.entity.User;
import com.sonder.yunpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Sonder
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-08-06 20:18:49
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


}
