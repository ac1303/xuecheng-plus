package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 小王的饭饭
 * @create 2023/1/18 15:16
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto {

    @NotNull(message = "未确定要修改的课程")
    @ApiModelProperty(value = "课程名称", required = true)
    private Long id;

}