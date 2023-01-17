package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author 小王的饭饭
 * @create 2023/1/15 18:57
 */
@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    @ApiModelProperty(value = "审核状态")
    private String auditStatus;
    //课程名称
    @ApiModelProperty(value = "课程名称")
    private String courseName;
    //发布状态
    @ApiModelProperty(value = "发布状态")
    private String publishStatus;

}
