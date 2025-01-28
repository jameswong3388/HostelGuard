package org.example.hvvs.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "medias")
public class Medias {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR) // Store UUID as string
    @Column(
            name = "id",
            columnDefinition = "CHAR(36)" // Match MySQL CHAR(36) format
    )
    private UUID id;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "collection")
    private String collection;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "disk")
    private String disk;

    @Column(name = "size")
    private Double size;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    // Constructors
    public Medias() {
        super();
    }

    public Medias(UUID id, String model, String modelId, String collection, String fileName,
                  String mimeType, String disk, Double size, Timestamp updatedAt, Timestamp createdAt) {
        super();
        this.id = id;
        this.model = model;
        this.modelId = modelId;
        this.collection = collection;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.disk = disk;
        this.size = size;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters


    public UUID getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getModelId() {
        return modelId;
    }

    public String getCollection() {
        return collection;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDisk() {
        return disk;
    }

    public Double getSize() {
        return size;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
