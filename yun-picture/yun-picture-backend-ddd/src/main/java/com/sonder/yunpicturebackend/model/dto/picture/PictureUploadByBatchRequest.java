package com.sonder.yunpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量导入图片请求
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 20;

    /**
     * 图片名称前缀, 默认为搜索词
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
