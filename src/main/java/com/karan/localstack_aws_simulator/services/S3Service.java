package com.karan.localstack_aws_simulator.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${s3.bucket.name}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // Create bucket if it does not exist
    public Bucket createBucketIfNotExists() {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            return amazonS3.createBucket(new CreateBucketRequest(bucketName, "ap-south-1"));
        }
        return amazonS3.listBuckets()
                .stream()
                .filter(bucket -> bucket.getName().equals(bucketName))
                .findFirst()
                .orElse(null);
    }

    // Stream upload large file
    public void uploadFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            amazonS3.putObject(new PutObjectRequest(bucketName, file.getName(), inputStream, metadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + file.getName(), e);
        }
    }

    // Stream download large file
    public S3ObjectInputStream downloadFileStream(String fileName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
        return s3Object.getObjectContent();
    }
}
