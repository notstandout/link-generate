package com.example.fileshare.controller;

import com.example.fileshare.dto.UploadResponse;
import com.example.fileshare.model.StoredFile;
import com.example.fileshare.service.FileStorageService;
import com.example.fileshare.service.FileValidationService;
import com.example.fileshare.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping
public class FileController {

    private final FileStorageService storageService;
    private final FileValidationService validationService;
    private final QrCodeService qrCodeService;

    public FileController(FileStorageService storageService, FileValidationService validationService, QrCodeService qrCodeService) {
        this.storageService = storageService;
        this.validationService = validationService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/api/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String resolvedType = validationService.validateAndResolveType(file.getOriginalFilename(), file.getContentType(), file.getSize());
        StoredFile saved = storageService.store(file, resolvedType);
        String publicUrl = baseUrl(request) + "/f/" + saved.getId();
        return new UploadResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getContentType(),
                saved.getSize(),
                publicUrl,
                qrCodeService.generateDataUrl(publicUrl, 280),
                validationService.resolvePreviewType(saved.getContentType())
        );
    }

    @GetMapping("/f/{id}")
    public ResponseEntity<Resource> fetchPublicFile(@PathVariable String id) {
        StoredFile file = storageService.getMetadataOrThrow(id);
        Resource resource = storageService.loadAsResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentLength(file.getSize());

        if ("text/html".equalsIgnoreCase(file.getContentType())) {
            // Uploaded HTML is forced as download to avoid rendering untrusted markup in browser.
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build());
        } else {
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build());
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping(value = "/api/files/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrCode(@PathVariable String id, HttpServletRequest request) {
        storageService.getMetadataOrThrow(id);
        String publicUrl = baseUrl(request) + "/f/" + id;
        return qrCodeService.generatePng(publicUrl, 280);
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());
    }
}
