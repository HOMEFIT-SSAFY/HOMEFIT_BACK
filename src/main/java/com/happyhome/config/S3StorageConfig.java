package com.happyhome.config;

import com.happyhome.transfer.service.S3TransferImageStorage;
import com.happyhome.transfer.service.TransferImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3StorageConfig {

    @Bean
    @ConditionalOnExpression("'${happyhome.s3.access-key:}' != '' && '${happyhome.s3.secret-key:}' != '' && '${happyhome.s3.bucket:}' != ''")
    S3Client happyHomeS3Client(
            @Value("${happyhome.s3.access-key}") String accessKey,
            @Value("${happyhome.s3.secret-key}") String secretKey,
            @Value("${happyhome.s3.region:ap-northeast-2}") String region
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${happyhome.s3.access-key:}' != '' && '${happyhome.s3.secret-key:}' != '' && '${happyhome.s3.bucket:}' != ''")
    TransferImageStorage s3TransferImageStorage(
            S3Client s3Client,
            @Value("${happyhome.s3.bucket}") String bucket,
            @Value("${happyhome.s3.region:ap-northeast-2}") String region,
            @Value("${happyhome.s3.public-base-url:}") String publicBaseUrl
    ) {
        String resolvedBaseUrl = StringUtils.hasText(publicBaseUrl) ? publicBaseUrl : "";
        return new S3TransferImageStorage(s3Client, bucket, region, resolvedBaseUrl);
    }
}
