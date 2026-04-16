package com.example.fileshare.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class FileValidationService {

    private static final long MAX_SIZE = 50L * 1024L * 1024L;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm", "video/ogg",
            "audio/mpeg", "audio/ogg", "audio/wav", "audio/webm",
            "application/pdf",
            "text/plain",
            "application/zip", "application/x-zip-compressed",
            "text/html"
    );

    private static final Map<String, String> EXTENSION_TO_TYPE = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("mp4", "video/mp4"),
            Map.entry("webm", "video/webm"),
            Map.entry("ogv", "video/ogg"),
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("ogg", "audio/ogg"),
            Map.entry("wav", "audio/wav"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("txt", "text/plain"),
            Map.entry("zip", "application/zip"),
            Map.entry("html", "text/html"),
            Map.entry("htm", "text/html")
    );

    public String validateAndResolveType(String filename, String contentType, long size) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("File is empty");
        }
        if (size > MAX_SIZE) {
            throw new IllegalArgumentException("File exceeds max size of 50MB");
        }

        String normalized = normalizeType(contentType);
        if (normalized != null && ALLOWED_CONTENT_TYPES.contains(normalized)) {
            return normalized;
        }

        String inferredFromExtension = inferFromFilename(filename);
        if (inferredFromExtension != null && ALLOWED_CONTENT_TYPES.contains(inferredFromExtension)) {
            return inferredFromExtension;
        }

        throw new IllegalArgumentException("Unsupported file type");
    }

    public String resolvePreviewType(String contentType) {
        if (contentType == null) {
            return "none";
        }
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.startsWith("audio/")) return "audio";
        if ("application/pdf".equals(contentType)) return "pdf";
        if ("text/plain".equals(contentType)) return "text";
        return "none";
    }

    private String normalizeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        return contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    private String inferFromFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        String extension = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        return EXTENSION_TO_TYPE.get(extension);
    }
}
