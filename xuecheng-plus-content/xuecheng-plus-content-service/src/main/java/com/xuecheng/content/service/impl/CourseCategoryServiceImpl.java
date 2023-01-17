package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 小王的饭饭
 * @create 2023/1/17 17:53
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        return getChildren(courseCategoryTreeDtos, id);
    }

    public List<CourseCategoryTreeDto> getChildren(List<CourseCategoryTreeDto> list, String id) {
        List<CourseCategoryTreeDto> collect = list.stream()
                .filter(p -> Objects.equals(p.getParentid(), id))
                .peek(courseCategoryTreeDto -> courseCategoryTreeDto.setChildrenTreeNodes(getChildren(list, courseCategoryTreeDto.getId())))
                .collect(Collectors.toList());
        return collect.size() == 0 ? null : collect;
    }

//    public static void main(String[] args) {
////        模拟数据测试getChildren
//        List<CourseCategoryTreeDto> test = new ArrayList<>();
//        test.add(new CourseCategoryTreeDto("1", "1", "0"));
//        test.add(new CourseCategoryTreeDto("2", "2", "0"));
//        test.add(new CourseCategoryTreeDto("3", "3", "0"));
//        test.add(new CourseCategoryTreeDto("1.1", "1.1", "1"));
//        test.add(new CourseCategoryTreeDto("1.2", "1.2", "1"));
//        test.add(new CourseCategoryTreeDto("1.3", "1.3", "1"));
//        test.add(new CourseCategoryTreeDto("1.1.1", "1.1.1", "1.1"));
//        test.add(new CourseCategoryTreeDto("1.1.2", "1.1.2", "1.1"));
//        test.add(new CourseCategoryTreeDto("2.1", "2.1", "2"));
//        test.add(new CourseCategoryTreeDto("2.2", "2.2", "2"));
//
//        CourseCategoryServiceImpl courseCategoryService = new CourseCategoryServiceImpl();
//        List<CourseCategoryTreeDto> children = courseCategoryService.getChildren(test, "0");
//        System.out.println(JSONObject.toJSONString(children));
//    }
}