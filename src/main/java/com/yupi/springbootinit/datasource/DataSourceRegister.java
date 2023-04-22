package com.yupi.springbootinit.datasource;

import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2023年04月21日 20:37
 */

@Component
public class DataSourceRegister {

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private PostDataSource postDataSource;

    private Map<String, DataSource> dataSourceTypeMap;

    @PostConstruct
    public void doInit() {
        dataSourceTypeMap = new HashMap(){{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
        }};
    }

    public DataSource getDataSourceType(String type) {
        if (dataSourceTypeMap == null) {
            return null;
        }
        return dataSourceTypeMap.get(type);
    }
}
