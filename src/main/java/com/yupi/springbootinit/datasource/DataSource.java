package com.yupi.springbootinit.datasource;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 适配器模式（提供搜索统一规范接口）
 */
public interface DataSource<T> {

    /**
     * 搜索
     *
     * @param searchText
     * @param pageSize
     * @param pageNum
     * @return
     */
    Page<T> doSearch(String searchText, long pageSize, long pageNum);
}
