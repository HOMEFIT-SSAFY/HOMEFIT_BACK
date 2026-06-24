package com.happyhome.transfer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3TransferImageStorageTest {

    @Test
    void storesTransferImageInS3AndReturnsPublicUrl() {
        S3Client s3Client = mock(S3Client.class);
        S3TransferImageStorage storage = new S3TransferImageStorage(
                s3Client,
                "homefitbucket",
                "ap-northeast-2",
                ""
        );
        MockMultipartFile file = new MockMultipartFile(
                "images",
                "room.jpg",
                "image/jpeg",
                "image-bytes".getBytes()
        );

        String url = storage.store(file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("homefitbucket");
        assertThat(request.key()).startsWith("transfers/").endsWith(".jpg");
        assertThat(request.contentType()).isEqualTo("image/jpeg");
        assertThat(url)
                .startsWith("https://homefitbucket.s3.ap-northeast-2.amazonaws.com/transfers/")
                .endsWith(".jpg");
    }
}
