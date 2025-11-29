package com.karan.localstack_aws_simulator.controller;

import com.karan.localstack_aws_simulator.services.S3Service;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/bucket")
    public String createBucket() {
        s3Service.createBucketIfNotExists();
        return "Bucket created!";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        // Save multipart file to temp file
        File tempFile = convertMultipartFileToTempFile(multipartFile);
        s3Service.uploadFile(tempFile);      // Stream upload
        tempFile.delete();                    // Clean up temp file
        return "File uploaded: " + multipartFile.getOriginalFilename();
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
        InputStreamResource resource = new InputStreamResource(s3Service.downloadFileStream(fileName));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    // Convert MultipartFile to temporary file on disk
    private File convertMultipartFileToTempFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("s3upload-", "-" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile;
    }
}
