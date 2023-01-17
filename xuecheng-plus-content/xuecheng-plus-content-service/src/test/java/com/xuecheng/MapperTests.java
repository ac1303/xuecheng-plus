package com.xuecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author 小王的饭饭
 * @create 2023/1/17 17:09
 */

@SpringBootTest
class MapperTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;


    @Test
    void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(JSONObject.toJSONString(courseCategoryTreeDtos));
    }

}