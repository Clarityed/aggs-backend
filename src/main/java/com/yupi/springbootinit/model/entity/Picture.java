package com.yupi.springbootinit.model.entity;

import lombok.Data;

/**
 * 图片对象，存储其他网站抓取来的图片对象
 *
 * @author: clarity
 * @date: 2023年04月19日 15:16
 */

@Data
public class Picture {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片标题
     */
    private String title;
}
