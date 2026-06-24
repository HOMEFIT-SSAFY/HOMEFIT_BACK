package com.happyhome.transfer.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnMissingBean(TransferImageStorage.class)
public class LocalTransferImageStorage implements TransferImageStorage {

    private final Path uploadRoot = Paths.get("uploads", "transfers").toAbsolutePath().normalize();

    @Override
    public String store(MultipartFile file) {
        try {
            Files.createDirectories(uploadRoot);
            String filename = UUID.randomUUID() + extension(file);
            Path target = uploadRoot.resolve(filename).normalize();
            file.transferTo(target);
            return "/uploads/transfers/" + filename;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String extension(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        int dotIndex = originalName.lastIndexOf('.');
        return dotIndex >= 0 ? originalName.substring(dotIndex) : "";
    }
}
