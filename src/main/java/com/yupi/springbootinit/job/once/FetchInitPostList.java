package com.yupi.springbootinit.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 项目启动，执行一次，加载其他网站的文章
 */

// 取消注释开启任务
// @Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
        // 1. 获取基本数据
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"_score\",\"sortOrder\":\"descend\",\"searchText\":\"java\",\"category\":\"文章\",\"reviewStatus\":1}";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();
        if (result == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 2. 将获取到的 json 转换成对象，报黄忽略，没有影响
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        if (map == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
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
        boolean validResult = postService.saveBatch(postList);
        if (validResult) {
            log.info("FetchInitPostList success, total = " + postList.size());
        } else {
            log.error("FetchInitPostList fail");
        }
    }
}
