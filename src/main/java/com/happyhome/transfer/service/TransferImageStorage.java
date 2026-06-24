package com.happyhome.transfer.service;

import org.springframework.web.multipart.MultipartFile;

public interface TransferImageStorage {

    String store(MultipartFile file);
}
