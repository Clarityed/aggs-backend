package com.yupi.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.datasource.*;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.search.SearchQueryRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.SearchVO;
import com.yupi.springbootinit.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 聚合搜索接口抽象出来的门面类
 *
 * @author: clarity
 * @date: 2023年04月21日 17:04
 */

@Component
@Slf4j
public class SearchFacade {

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private DataSourceRegister dataSourceRegister;

    /**
     * 聚合搜索
     *
     * @param searchQueryRequest
     * @param request
     * @return
     */
    public SearchVO searchAllVOByPage(@RequestBody SearchQueryRequest searchQueryRequest,
                                      HttpServletRequest request) {
        // 模糊查询用户
        String searchText = searchQueryRequest.getSearchText();
        long pageNum = searchQueryRequest.getCurrent();
        long pageSize = searchQueryRequest.getPageSize();
        // 搜索的数据类型
        String type = searchQueryRequest.getType();
        // 聚合搜索返回值
        SearchVO searchVO = new SearchVO();
        // 1. 如果 type 为空，那么搜索出所有数据。
        if (StringUtils.isBlank(type)) {
            // 并发执行
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                // 模糊查询用户
                return userDataSource.doSearch(searchText, pageSize, pageNum);
            });
            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                // 模糊查询文章
                return postDataSource.doSearch(searchText, pageSize, pageNum);
            });
            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                // 模糊查询图片
                return pictureDataSource.doSearch(searchText, pageSize, pageNum);
            });
            // 代码上面的任务都执行完成
            CompletableFuture.allOf(userTask, postTask, pictureTask).join();
            try {
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                Page<Picture> picturePage = pictureTask.get();
                searchVO.setUserVOList(userVOPage.getRecords());
                searchVO.setPostVOList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
                return searchVO;
            } catch (Exception e) {
                log.error("聚合查询异常" + e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "聚合查询异常");
            }
        } else {
            // 2. 如果 type 不为空
            // 如果 type 合法，查询出对于的数据（采用枚举类判断是否合法）
            // 否则报错
            SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
            if (searchTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "类型不合法");
            }
            DataSource<?> dataSource = dataSourceRegister.getDataSourceType(type);
            Page<?> page = dataSource.doSearch(searchText, pageSize, pageNum);
            searchVO.setDataList((List<Object>) page.getRecords());
            return searchVO;
        }
    }
}
