package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
 @Service
public class MediaFileServiceImpl implements MediaFileService {
 @Autowired
 MinioClient minioClient;
  @Autowired
 MediaFilesMapper mediaFilesMapper;

  @Autowired
  MediaFileService mediaFileService;

 @Value("${minio.bucket.files}")
 private String bucket_Files;


 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

// /**
//  * @param companyId
//  * @param uploadFileParamsDto 上传文件信息
//  * @param bytes
//  * @param folder              文件目录,如果不传则默认年、月、日
//  * @param objectName
//  * @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件结果
//  * @description 上传文件
//  */
// @Transactional
// @Override
// public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
//
//  //生成文件id，文件的md5值
//  String fileId = DigestUtils.md5Hex(bytes);
//  //文件名称
//  String filename = uploadFileParamsDto.getFilename();
//  //构造objectname
//  if (StringUtils.isEmpty(objectName)) {
//   objectName = fileId + filename.substring(filename.lastIndexOf("."));
//  }
//  if (StringUtils.isEmpty(folder)) {
//   //通过日期构造文件存储路径
//   folder = getFileFolder(new Date(), true, true, true);
//  } else if (folder.indexOf("/") < 0) {
//   folder = folder + "/";
//  }
//  //对象名称
//  objectName = folder + objectName;
//  MediaFiles mediaFiles = null;
//  try {
//   //转为流
//   ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//
//   PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket_Files).object(objectName)
//           //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
//           .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
//           .contentType(uploadFileParamsDto.getContentType())
//           .build();
//
//
//   minioClient.putObject(putObjectArgs);
//   //从数据库查询文件
//   mediaFiles = mediaFilesMapper.selectById(fileId);
//   if (mediaFiles == null) {
//    mediaFiles = new MediaFiles();
//    //拷贝基本信息
//    BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
//    //设置文件id
//    mediaFiles.setId(fileId);
//    mediaFiles.setFileId(fileId);
//    mediaFiles.setCompanyId(companyId);
//    mediaFiles.setUrl("/" + bucket_Files + "/" + objectName);
//    mediaFiles.setBucket(bucket_Files);
//    mediaFiles.setCreateDate(LocalDateTime.now());
//    mediaFiles.setStatus("1");
//    //保存文件信息到文件表
//    int insert = mediaFilesMapper.insert(mediaFiles);
//    if (insert < 0) {
//     XueChengPlusException.cast("保存文件信息失败");
//    }
//    UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
//    BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
//    return uploadFileResultDto;
//   }
//  } catch (Exception e) {
//   e.printStackTrace();
//   XueChengPlusException.cast("上传过程中出错");
//  }
//  return null;
// }


 //根据日期拼接目录
 private String getFileFolder(Date date, boolean year, boolean month, boolean day){
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  //获取当前日期字符串
  String dateString = sdf.format(new Date());
  //取出年、月、日
  String[] dateStringArray = dateString.split("-");
  StringBuffer folderString = new StringBuffer();
  if(year){
   folderString.append(dateStringArray[0]);
   folderString.append("/");
  }
  if(month){
   folderString.append(dateStringArray[1]);
   folderString.append("/");
  }
  if(day){
   folderString.append(dateStringArray[2]);
   folderString.append("/");
  }
  return folderString.toString();
 }



 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

  //生成文件id，文件的md5值
  String fileId = DigestUtils.md5Hex(bytes);
  //文件名称
  String filename = uploadFileParamsDto.getFilename();
  //构造objectname
  if (StringUtils.isEmpty(objectName)) {
   objectName = fileId + filename.substring(filename.lastIndexOf("."));
  }
  if (StringUtils.isEmpty(folder)) {
   //通过日期构造文件存储路径
   folder = getFileFolder(new Date(), true, true, true);
  } else if (folder.indexOf("/") < 0) {
   folder = folder + "/";
  }
  //对象名称
  objectName = folder + objectName;
  MediaFiles mediaFiles = null;
  try {
   //上传至文件系统
   addMediaFilesToMinIO(bytes,bucket_Files,objectName,uploadFileParamsDto.getContentType());
   //写入文件表
   mediaFiles = mediaFileService.addMediaFilesToDb(companyId,fileId,uploadFileParamsDto,bucket_Files,objectName);
   UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
   BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
   return uploadFileResultDto;
  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.cast("上传过程中出错");
  }
  return null;

 }

 /**
  * @description 将文件写入minIO
  * @param bytes  文件字节数组
  * @param bucket  桶
  * @param objectName 对象名称
  * @param contentType  内容类型
  * @return void
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 @Override
 public void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName, String contentType) {
  //转为流
  ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

  try {
   PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket).object(objectName)
           //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
           .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
           .contentType(contentType)
           .build();

   minioClient.putObject(putObjectArgs);
  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.cast("上传文件到文件系统出错");
  }
 }

 /**
  * @description 将文件信息添加到文件表
  * @param companyId  机构id
  * @param fileMd5  文件md5值
  * @param uploadFileParamsDto  上传文件的信息
  * @param bucket  桶
  * @param objectName 对象名称
  * @return com.xuecheng.media.model.po.MediaFiles
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 @Override
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){

  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert < 0) {
    XueChengPlusException.cast("保存文件信息失败");
   }

  }
  return mediaFiles;

 }


}
