package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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
 @Log4j2
public class MediaFileServiceImpl implements MediaFileService {
 @Autowired
 MinioClient minioClient;
  @Autowired
 MediaFilesMapper mediaFilesMapper;

  @Autowired
  MediaFileService mediaFileService;

 @Value("${minio.bucket.files}")
 private String bucket_Files;

 @Value("${minio.bucket.videofiles}")
 private String bucket_videoFiles;


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
   mediaFiles.setFilePath(objectName);
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

 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //查询文件信息
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles != null) {
   //桶
   String bucket = mediaFiles.getBucket();
   //存储目录
   String filePath = mediaFiles.getFilePath();
   //文件流
   InputStream stream = null;
   try {
    stream = minioClient.getObject(
            GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build());

    if (stream != null) {
     //文件已存在
     return RestResponse.success(true);
    }
   } catch (Exception ignored) {

   }
  }
  //文件不存在
  return RestResponse.success(false);
 }

 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {


  //得到分块文件的目录路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //得到分块文件的路径
  String chunkFilePath = chunkFileFolderPath + chunk;

  try {
   //将文件存储至minIO
   addMediaFilesToMinIO(bytes, bucket_videoFiles,chunkFilePath,"application/octet-stream");

  } catch (Exception ex) {
   ex.printStackTrace();
   XueChengPlusException.cast("上传过程出错请重试");
  }
  return RestResponse.success();
 }


 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

  //得到分块文件目录
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //得到分块文件的路径
  String chunkFilePath = chunkFileFolderPath + chunkIndex;

  //文件流
  InputStream fileInputStream = null;
  try {
   fileInputStream = minioClient.getObject(
           GetObjectArgs.builder()
                   .bucket(bucket_videoFiles)
                   .object(chunkFilePath)
                   .build());

   if (fileInputStream != null) {
    //分块已存在
    return RestResponse.success(true);
   }
  } catch (Exception ignored) {
  }
  //分块未存在
  return RestResponse.success(false);
 }

 //得到分块文件的目录
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
 }


 //检查所有分块是否上传完毕
 private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
  //得到分块文件的目录路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  File[] files = new File[chunkTotal];
  //检查分块文件是否上传完毕
  for (int i = 0; i < chunkTotal; i++) {
   String chunkFilePath = chunkFileFolderPath + i;
   //下载文件
   File chunkFile =null;
   try {
    chunkFile = File.createTempFile("chunk" + i, null);
   } catch (IOException e) {
    e.printStackTrace();
    XueChengPlusException.cast("下载分块时创建临时文件出错");
   }
   downloadFileFromMinIO(chunkFile,bucket_videoFiles,chunkFilePath);
   files[i]=chunkFile;
  }
  return files;
 }
 //根据桶和文件路径从minio下载文件
 public File downloadFileFromMinIO(File file,String bucket,String objectName){
  InputStream fileInputStream = null;
  OutputStream fileOutputStream = null;
  try {
   fileInputStream = minioClient.getObject(
           GetObjectArgs.builder()
                   .bucket(bucket)
                   .object(objectName)
                   .build());
   try {
    fileOutputStream = new FileOutputStream(file);
    IOUtils.copy(fileInputStream, fileOutputStream);

   } catch (IOException e) {
    XueChengPlusException.cast("下载文件"+objectName+"出错");
   }
  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.cast("文件不存在"+objectName);
  } finally {
   if (fileInputStream != null) {
    try {
     fileInputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
   if (fileOutputStream != null) {
    try {
     fileOutputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return file;
 }



 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

  String fileName = uploadFileParamsDto.getFilename();
  //下载所有分块文件
  File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
  //扩展名
  String extName = fileName.substring(fileName.lastIndexOf("."));
  //创建临时文件作为合并文件
  File mergeFile = null;
  try {
   mergeFile = File.createTempFile(fileMd5, extName);
  } catch (IOException e) {
   XueChengPlusException.cast("合并文件过程中创建临时文件出错");
  }

  try {
   //开始合并
   byte[] b = new byte[1024];
   try(RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");) {
    for (File chunkFile : chunkFiles) {
     try (FileInputStream chunkFileStream = new FileInputStream(chunkFile);) {
      int len = -1;
      while ((len = chunkFileStream.read(b)) != -1) {
       //向合并后的文件写
       raf_write.write(b, 0, len);
      }
     }
    }
   } catch (IOException e) {
    e.printStackTrace();
    XueChengPlusException.cast("合并文件过程中出错");
   }
   log.debug("合并文件完成{}",mergeFile.getAbsolutePath());
   uploadFileParamsDto.setFileSize(mergeFile.length());

   try (InputStream mergeFileInputStream = new FileInputStream(mergeFile);) {
    //对文件进行校验，通过比较md5值
    String newFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
    if (!fileMd5.equalsIgnoreCase(newFileMd5)) {
     //校验失败
     XueChengPlusException.cast("合并文件校验失败");
    }
    log.debug("合并文件校验通过{}",mergeFile.getAbsolutePath());
   } catch (Exception e) {
    e.printStackTrace();
    //校验失败
    XueChengPlusException.cast("合并文件校验异常");
   }

   //将临时文件上传至minio
   String mergeFilePath = getFilePathByMd5(fileMd5, extName);
   try {

    //上传文件到minIO
    addMediaFilesToMinIO(mergeFile.getAbsolutePath(), bucket_videoFiles, mergeFilePath);
    log.debug("合并文件上传MinIO完成{}",mergeFile.getAbsolutePath());
   } catch (Exception e) {
    e.printStackTrace();
    XueChengPlusException.cast("合并文件时上传文件出错");
   }

   //入数据库
   MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, mergeFilePath);
   if (mediaFiles == null) {
    XueChengPlusException.cast("媒资文件入库出错");
   }

   return RestResponse.success();
  } finally {
   //删除临时文件
   for (File file : chunkFiles) {
    try {
     file.delete();
    } catch (Exception e) {

    }
   }
   try {
    mergeFile.delete();
   } catch (Exception e) {

   }
  }
 }


 private String getFilePathByMd5(String fileMd5,String fileExt){
  return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
 }


 //将文件上传到minIO，传入文件绝对路径
 public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
  try {
   minioClient.uploadObject(
           UploadObjectArgs.builder()
                   .bucket(bucket)
                   .object(objectName)
                   .filename(filePath)
                   .build());
  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.cast("上传文件到文件系统出错");
  }
 }

@Override
 public MediaFiles getFileById(String id) {
  return mediaFilesMapper.selectById(id);
 }


}
