import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author 小王的饭饭
 * @create 2023/1/19 17:54
 */
public class MinIOTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.223.128:9006")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    //上传文件
    public static void upload() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("testbucket").build());
            //检查testbucket桶是否创建，没有创建自动创建
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("testbucket").build());
            } else {
                System.out.println("Bucket 'testbucket' already exists.");
            }
            //上传1.mp4
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("1.mp4")
                            .filename("C:\\Users\\安宸\\Documents\\1.mp4")
                            .build());
            //上传1.avi,上传到avi子目录
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("avi/1.avi")
                            .filename("C:\\Users\\安宸\\Documents\\1.mp4")
                            .build());
            System.out.println("上传成功");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }

    }
    public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        upload();
    }


}
