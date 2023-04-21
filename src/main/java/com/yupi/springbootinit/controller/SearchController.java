package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.post.PictureQueryRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.dto.search.SearchQueryRequest;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.SearchVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.PictureService;
import com.yupi.springbootinit.service.PostService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 聚合搜索接口
 *
 * @author: clarity
 * @date: 2023年04月21日 11:25
 */

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private PictureService pictureService;

    /**
     * 聚合搜索（从 ES 查询，封装类）
     *
     * @param searchQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/aggs")
    public BaseResponse<SearchVO> searchPictureVOByPage(@RequestBody SearchQueryRequest searchQueryRequest,
                                                             HttpServletRequest request) {
        // 模糊查询用户
        String searchText = searchQueryRequest.getSearchText();
        long pageNum = searchQueryRequest.getCurrent();
        long pageSize = searchQueryRequest.getPageSize();

        // 并发执行
        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            // 模糊查询用户
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setCurrent(pageNum);
            userQueryRequest.setPageSize(pageSize);
            userQueryRequest.setUserName(searchText);
            return userService.listUserVOByPage(userQueryRequest);
        });

        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            // 模糊查询文章
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setCurrent(pageNum);
            postQueryRequest.setPageSize(pageSize);
            postQueryRequest.setSearchText(searchText);
            return postService.listPostVOByPage(postQueryRequest, request);
        });

        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
            // 模糊查询图片
            return pictureService.searchPicture(searchText, pageSize, pageNum);
        });

        // 代码上面的任务都执行完成
        CompletableFuture.allOf(userTask, postTask, pictureTask).join();

        try {
            Page<UserVO> userVOPage = userTask.get();
            Page<PostVO> postVOPage = postTask.get();
            Page<Picture> picturePage = pictureTask.get();
            SearchVO searchVO = new SearchVO();
            searchVO.setUserVOList(userVOPage.getRecords());
            searchVO.setPostVOList(postVOPage.getRecords());
            searchVO.setPictureList(picturePage.getRecords());
            return ResultUtils.success(searchVO);
        } catch (Exception e) {
            log.error("聚合查询异常" + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "聚合查询异常");
        }
    }
}
