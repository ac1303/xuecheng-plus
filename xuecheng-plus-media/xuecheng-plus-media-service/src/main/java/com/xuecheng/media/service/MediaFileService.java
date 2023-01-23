package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 媒体文件服务
 *
 * @author 范书华
 * @description 媒资文件管理业务类
 * @date 2023/01/20
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
 */
 PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  *
  * @param uploadFileParamsDto 上传文件信息
  * @param folder              文件目录,如果不传则默认年、月、日
  * @param companyId           公司标识
  * @param bytes               字节
  * @param objectName          对象名称
  * @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件结果
  * @description 上传文件
  */
 UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);


 /**
  * 媒体文件添加到数据库
  *
  * @param companyId           公司标识
  * @param fileMd5             文件md5
  * @param uploadFileParamsDto 上传文件params dto
  * @param bucket              桶
  * @param objectName          对象名称
  * @return {@link MediaFiles}
  */
 MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

 /**
  * 将媒体文件添加到minio
  *
  * @param bytes       字节
  * @param bucket      桶
  * @param objectName  对象名称
  * @param contentType 内容类型
  */
 void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName, String contentType);

 /**
  * @description 检查文件是否存在
  * @param fileMd5 文件的md5
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  */
 RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * @description 检查分块是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分块序号
  * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * @description 上传分块
  * @param fileMd5  文件md5
  * @param chunk  分块序号
  * @param bytes  文件字节
  * @return com.xuecheng.base.model.RestResponse
  */
 public RestResponse uploadChunk(String fileMd5,int chunk,byte[] bytes);

 /**
  * @description 合并分块
  * @param companyId  机构id
  * @param fileMd5  文件md5
  * @param chunkTotal 分块总和
  * @param uploadFileParamsDto 文件信息
  * @return com.xuecheng.base.model.RestResponse
  */
 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

 /**
  * @description 根据id查询文件信息
  * @param id  文件id
  * @return com.xuecheng.media.model.po.MediaFiles 文件信息
  */
 public MediaFiles getFileById(String id);

}
