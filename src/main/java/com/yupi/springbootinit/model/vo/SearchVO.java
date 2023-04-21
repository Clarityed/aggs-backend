package com.yupi.springbootinit.model.vo;

import com.google.gson.Gson;
import com.yupi.springbootinit.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索结果封装类
 *
 * @author: clarity
 * @date: 2023年04月21日 11:41
 */

@Data
public class SearchVO implements Serializable {

    private final static Gson GSON = new Gson();

    /**
     * 文章列表
     */
    private List<PostVO> postVOList;

    /**
     * 文章列表
     */
    private List<UserVO> userVOList;

    /**
     * 文章列表
     */
    private List<Picture> pictureList;
}
