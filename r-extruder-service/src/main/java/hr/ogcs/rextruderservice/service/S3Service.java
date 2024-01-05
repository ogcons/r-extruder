package hr.ogcs.rextruderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class S3Service {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private final S3Client s3Client;
    private final String bucketName;

    @Value("${cloud.aws.s3.bucket}")
    private String s3Bucket;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadFileToS3(MultipartFile wordFile) throws IOException {
        try {
            String objectKey = counter.getAndIncrement() + "_" + wordFile.getOriginalFilename();

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(objectKey)
                    .build(), RequestBody.fromInputStream(wordFile.getInputStream(), wordFile.getSize()));

            return objectKey;
        } catch (S3Exception e) {
            throw new IOException("Failed to download Word document from S3", e);
        }
    }

    public byte[] downloadFileFromS3(String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            log.error("Failed to download Word document from S3", e);
            throw new IOException("Failed to download Word document from S3", e);
        }
    }

    public List<String> listFilesOfBucket() throws IOException {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            List<S3Object> s3Objects = listObjectsResponse.contents();

            return s3Objects.stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (S3Exception e) {
            throw new IOException("Failed to list files in S3 bucket", e);
        }
    }
}
