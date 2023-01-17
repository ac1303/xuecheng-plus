package com.xuecheng;

import com.alibaba.fastjson.JSONObject;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById("22");
        Assertions.assertNotNull(courseBase);
    }
    @Test
    void testCourseBaseInfoService() {
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(
                new PageParams(1, 10),
                new QueryCourseParamsDto());
        System.out.println(courseBasePageResult);
    }
    @Test
    void testCourseCategoryService() {
        List<CourseCategoryTreeDto> x = courseCategoryService.queryTreeNodes("1");
        System.out.println(JSONObject.toJSONString(x));
    }

}
