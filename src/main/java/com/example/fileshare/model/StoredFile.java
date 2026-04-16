package com.example.fileshare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @Column(length = 40, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storageName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private Instant createdAt;

    protected StoredFile() {
    }

    public StoredFile(String id, String originalName, String storageName, String contentType, long size, Instant createdAt) {
        this.id = id;
        this.originalName = originalName;
        this.storageName = storageName;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
