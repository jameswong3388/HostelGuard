package org.example.hvvs.modules.common.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.Medias;
import org.example.hvvs.modules.common.repository.MediaRepository;
import org.example.hvvs.utils.FileStorageUtil;
import org.example.hvvs.utils.FileStorageUtil.StorageResult;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MediaServiceImpl implements MediaService {

    @Inject
    private MediaRepository mediaRepository;

    @Inject
    private FileStorageUtil fileStorageUtil;

    @Override
    @Transactional
    public Medias uploadFile(Part file, String model, String modelId, String collection) throws IOException {
        try {
            StorageResult storageResult = fileStorageUtil.storeFile(file, collection);

            Timestamp now = Timestamp.from(Instant.now());
            Medias media = new Medias();

            media.setModel(model);
            media.setModelId(modelId);
            media.setCollection(collection);
            media.setFileName(file.getSubmittedFileName());
            media.setMimeType(file.getContentType());
            media.setDisk(storageResult.getRelativePath());
            media.setSize((double) file.getSize());
            media.setCreatedAt(now);
            media.setUpdatedAt(now);

            return mediaRepository.save(media);
        } catch (Exception e) {
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Optional<Medias> findById(UUID id) {
        try {
            return mediaRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public List<Medias> findByModelAndModelId(String model, String modelId) {
        return mediaRepository.findByModelAndModelId(model, modelId);
    }

    @Override
    @Transactional
    public List<Medias> findByCollection(String collection) {
        return mediaRepository.findByCollection(collection);
    }

    @Override
    @Transactional
    public void deleteMedia(UUID id) throws IOException {
        try {
            Optional<Medias> mediaOptional = mediaRepository.findById(id);
            if (mediaOptional.isPresent()) {
                Medias media = mediaOptional.get();
                fileStorageUtil.deleteFile(media.getDisk());
                mediaRepository.delete(media);
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete media: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteByModelAndModelId(String model, String modelId) throws IOException {
        try {
            List<Medias> medias = mediaRepository.findByModelAndModelId(model, modelId);
            for (Medias media : medias) {
                fileStorageUtil.deleteFile(media.getDisk());
            }
            mediaRepository.deleteByModelAndModelId(model, modelId);
        } catch (Exception e) {
            throw new IOException("Failed to delete media by model and modelId: " + e.getMessage(), e);
        }
    }
} 