package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author 小王的饭饭
 * @create 2023/1/17 16:26
 */
@Data
@NoArgsConstructor
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
//            <id     column="one_id"        property="id" />
//        <result column="one_name"      property="name" />
//        <result column="one_label"     property="label" />
//        <result column="one_parentid"  property="parentid" />
//        <result column="one_orderby"   property="orderby" />

    private List<CourseCategoryTreeDto> childrenTreeNodes;
    private String id;
    private String name;
    private String label;
    private String parentid;
    private Integer orderby;

    public CourseCategoryTreeDto(String id,String name,String parentid) {
        this.id = id;
        this.name = name;
        this.parentid = parentid;
    }
}
