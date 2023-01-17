package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

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

}
