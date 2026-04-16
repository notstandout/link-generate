package com.example.fileshare.dto;

public record UploadResponse(
        String id,
        String fileName,
        String contentType,
        long size,
        String publicUrl,
        String qrCodeDataUrl,
        String previewType
) {
}
