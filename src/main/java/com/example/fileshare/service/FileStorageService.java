package com.example.fileshare.service;

import com.example.fileshare.model.StoredFile;
import com.example.fileshare.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class FileStorageService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StoredFileRepository repository;
    private final Path uploadRoot;

    public FileStorageService(StoredFileRepository repository, @Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.repository = repository;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadRoot);
    }

    public StoredFile store(MultipartFile file, String resolvedContentType) {
        String id = newId();
        String originalName = StringUtils.cleanPath(Optional.ofNullable(file.getOriginalFilename()).orElse("file"));
        String extension = extensionOf(originalName);
        String storageName = id + extension;

        try {
            Path target = uploadRoot.resolve(storageName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            StoredFile metadata = new StoredFile(
                    id,
                    originalName,
                    storageName,
                    resolvedContentType,
                    file.getSize(),
                    Instant.now()
            );
            return repository.save(metadata);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store file", ex);
        }
    }

    public StoredFile getMetadataOrThrow(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public Resource loadAsResource(StoredFile file) {
        Path path = uploadRoot.resolve(file.getStorageName()).normalize();
        return new FileSystemResource(path);
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return "";
        return fileName.substring(dot);
    }

    private String newId() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
