package com.happyhome.transfer.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3TransferImageStorage implements TransferImageStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String publicBaseUrl;

    public S3TransferImageStorage(S3Client s3Client, String bucket, String region, String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public String store(MultipartFile file) {
        String key = "transfers/" + UUID.randomUUID() + extension(file);
        try {
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentLength(file.getSize());
            if (StringUtils.hasText(file.getContentType())) {
                request.contentType(file.getContentType());
            }
            s3Client.putObject(request.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return publicUrl(key);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String publicUrl(String key) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extension(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        int dotIndex = originalName.lastIndexOf('.');
        return dotIndex >= 0 ? originalName.substring(dotIndex) : "";
    }
}
