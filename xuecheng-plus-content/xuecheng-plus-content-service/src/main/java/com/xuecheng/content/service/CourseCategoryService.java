package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author 小王的饭饭
 * @create 2023/1/17 17:52
 */
public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
