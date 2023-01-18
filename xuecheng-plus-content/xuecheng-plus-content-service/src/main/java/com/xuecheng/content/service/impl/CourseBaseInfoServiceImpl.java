package com.xuecheng.content.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseMarketService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 小王的饭饭
 * @create 2023/1/17 12:53
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseMarketService courseMarketService;

    /**
     * @description 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 条件条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = Wrappers.lambdaQuery();

//        模糊查询
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());
//        审核状态
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());
//        课程发布状态
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamsDto.getPublishStatus());

//        分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = pageResult.getRecords();
        long total = pageResult.getTotal();
        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * @param companyId    教学机构id
     * @param dto 课程基本信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto dto) {

        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();
        //课程营销信息
        //先根据课程id查询营销信息
        CourseMarket courseMarketNew = courseMarketMapper.selectById(courseId);
        if(courseMarketNew!=null){
            throw new RuntimeException("收费课程价格不能为空");
        }
        courseMarketNew =  new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);
        //收费规则
        String charge = dto.getCharge();

        //收费课程必须写价格
        if ("201001".equals(charge)) {
            BigDecimal price = dto.getPrice();
            if (ObjectUtils.isEmpty(price) || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new XueChengPlusException("收费课程价格不能为空");
            }
            courseMarketNew.setPrice(dto.getPrice().floatValue());
            courseMarketNew.setOriginalPrice(dto.getOriginalPrice().floatValue());
        }

        //插入课程营销信息
        int insert1 = courseMarketMapper.insert(courseMarketNew);

        if(insert1<=0 || insert1<=0){
            throw new RuntimeException("新增课程基本信息失败");
        }
        //添加成功
        //返回添加的课程信息
        return getCourseBaseInfo(courseId);

    }
    //根据课程id查询课程基本信息，包括基本信息和营销信息
//    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
//
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
//
//        if(courseBase == null){
//            return null;
//        }
//        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
//        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
//        if(courseMarket != null){
//            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
//        }
//
//        //查询分类名称
//        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
//        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
//        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
//        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
//
//        return courseBaseInfoDto;
//
//    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        LambdaQueryWrapper<CourseMarket> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseMarket::getId,courseId);
        CourseMarket courseMarket = courseMarketMapper.selectOne(queryWrapper);

        if(courseBase == null || courseMarket == null){
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        courseBaseInfoDto.setPrice(courseMarket.getPrice());
        courseBaseInfoDto.setCharge(courseMarket.getCharge());

        return courseBaseInfoDto;

    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //课程id
        Long courseId = dto.getId();
        CourseBase courseBaseUpdate = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBaseUpdate.getCompanyId())){
            XueChengPlusException.cast("只允许修改本机构的课程");
        }
        BeanUtils.copyProperties(dto,courseBaseUpdate);
        courseBaseUpdate.setId(courseId);
        courseBaseUpdate.setCompanyId(companyId);
        //更新
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBaseUpdate);

        //查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseMarket==null){
            courseMarket = new CourseMarket();
        }

        courseMarket.setId(courseId);
        courseMarket.setCharge(dto.getCharge());
        //收费规则
        String charge = dto.getCharge();

        //收费课程必须写价格
        if ("201001".equals(charge)) {
            BigDecimal price = dto.getPrice();
            if (ObjectUtils.isEmpty(price)) {
                throw new XueChengPlusException("收费课程价格不能为空");
            }
            courseMarket.setPrice(dto.getPrice().floatValue());
        }
        //保存课程营销信息，没有则添加，有则更新
        courseMarketService.saveOrUpdate(courseMarket);

        //返回添加的课程信息
        return getCourseBaseInfo(courseId);
    }
}
