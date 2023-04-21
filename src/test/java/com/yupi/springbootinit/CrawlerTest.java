package com.yupi.springbootinit;
import java.io.IOException;
import java.util.Date;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 爬虫测试
 *
 * @author: clarity
 * @date: 2023年04月19日 11:17
 */

@SpringBootTest
public class CrawlerTest {

    @Test
    void testFetchPicture() throws IOException {
        int current = 1;
        String url = "https://cn.bing.com/images/search?q=原神图片&first=" + current;
        Document doc = Jsoup.connect(url).get();
        System.out.println(doc);
        // 通过样式选择器获取在该样式下的所有元素
        Elements elements = doc.select(".iuscp.isv");
        // 图片只要地址和标题，创建实体存储，不用存到数据库，需要的时候调用即可。
        // 并且创建列表存储抓住来的图片对象
        List<Picture> pictureList = new ArrayList<>();
        for (Element element : elements) {
            // 获取图片地址
            String m = element.select(".iusc").get(0).attr("m");
            // m 是 json 格式的数据
            System.out.println(m);
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            System.out.println(murl);
            // 获取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            System.out.println(title);
            Picture picture = new Picture();
            picture.setUrl(murl);
            picture.setTitle(title);
            pictureList.add(picture);
        }
        System.out.println(pictureList);
    }

    @Test
    void testFetchPost() {
        // 1. 获取基本数据
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"_score\",\"sortOrder\":\"descend\",\"searchText\":\"java\",\"category\":\"文章\",\"reviewStatus\":1}";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();
        System.out.println(result);
        if (result == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 2. 将获取到的 json 转换成对象，报黄忽略，没有影响
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        if (map == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        System.out.println(map);
        Integer codeNum = (Integer) map.get("code");
        if (codeNum != 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        if (records.size() <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 3. 定义列表，用于存储本系统要使用到的文章列表
        List<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;
            Post post = new Post();
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1648163702345625602L);
            post.setCreateTime(new Date());
            post.setUpdateTime(new Date());
            postList.add(post);
        }
        System.out.println(postList);
    }
}
