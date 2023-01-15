package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author 小王的饭饭
 * @create 2023/1/15 18:54
 * @description 分页查询通用参数
 * @version 1.0
 */
@Data
@ToString
public class PageParams {

    //当前页码默认值
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    //每页记录数默认值
    public static final long DEFAULT_PAGE_SIZE = 10L;

    //当前页码
    @ApiModelProperty(value = "当前页码")
    private Long pageNo = DEFAULT_PAGE_CURRENT;

    //每页记录数默认值
    @ApiModelProperty(value = "每页记录数")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    public PageParams(){

    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }



}
