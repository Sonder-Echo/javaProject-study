package com.sonder.yunpicture.infrastructure.api.imagesearch;

import com.sonder.yunpicture.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.sonder.yunpicture.infrastructure.api.imagesearch.sub.GetImageFirstUrlApi;
import com.sonder.yunpicture.infrastructure.api.imagesearch.sub.GetImageListApi;
import com.sonder.yunpicture.infrastructure.api.imagesearch.sub.GetImagePageUrlApi;

import java.util.List;

public class ImageSearchApiFacade {

    /**
     * 搜索图片
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        String imageUrl = "https://ts1.tc.mm.bing.net/th/id/R-C.e8bafd9213e3fc64d0841d1b04c30286?rik=5irXG0yFJvndtg&riu=http%3a%2f%2fimg.52desk.com%2ftp%2f0014537fZp3.jpg&ehk=%2fnp2SU863TPqYZUMfygrIrhWQFJDGrUmvKI3g9LwbDQ%3d&risl=&pid=ImgRaw&r=0";
        List<ImageSearchResult> imageList = searchImage(imageUrl);
        System.out.println(imageList);
    }
}
