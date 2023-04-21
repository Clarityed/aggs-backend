package com.yupi.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Override
    public Page<Picture> searchPicture(String searchText, long pageSize, long pageNum) {
        long current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("查询图片异常" + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // System.out.println(doc);
        // 通过样式选择器获取在该样式下的所有元素
        Elements elements = doc.select(".iuscp.isv");
        // 图片只要地址和标题，创建实体存储，不用存到数据库，需要的时候调用即可。
        // 并且创建列表存储抓住来的图片对象
        List<Picture> pictureList = new ArrayList<>();
        for (Element element : elements) {
            // 获取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            // m 是 json 格式的数据
            // System.out.println(m);
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            // System.out.println(murl);
            // 获取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            // System.out.println(title);
            Picture picture = new Picture();
            picture.setUrl(murl);
            picture.setTitle(title);
            pictureList.add(picture);
            if (pictureList.size() >= pageSize) {
                break;
            }
        }
        // System.out.println(pictureList);
        Page<Picture> picturePage = new PageDTO<>(pageSize, pageNum);
        picturePage.setRecords(pictureList);
        return picturePage;
    }
}




