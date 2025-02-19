package org.example.hvvs.modules.common.service;

import jakarta.ejb.*;
import jakarta.servlet.http.Part;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.MediasFacade;
import org.example.hvvs.utils.FileStorageUtil;
import org.example.hvvs.utils.FileStorageUtil.StorageResult;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
@Local(MediaService.class)
public class MediaServiceImpl implements MediaService {

    @EJB
    private MediasFacade mediasFacade;

    @EJB
    private FileStorageUtil fileStorageUtil;

    @Override
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
            
            if (media.getId() == null) {
                mediasFacade.create(media);
            } else {
                mediasFacade.edit(media);
            }
            return media;
        } catch (Exception e) {
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Optional<Medias> findById(UUID id) {
        return Optional.ofNullable(mediasFacade.find(id));
    }

    @Override
    public List<Medias> findByModelAndModelId(String model, String modelId) {
        return mediasFacade.findByModelAndModelId(model, modelId);
    }

    @Override
    public List<Medias> findByCollection(String collection) {
        return mediasFacade.findByCollection(collection);
    }

    @Override
    public void deleteMedia(UUID id) throws IOException {
        try {
            Optional<Medias> mediaOptional = this.findById(id);
            if (mediaOptional.isPresent()) {
                Medias media = mediaOptional.get();
                fileStorageUtil.deleteFile(media.getDisk());
                mediasFacade.remove(media);
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete media: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByModelAndModelId(String model, String modelId) throws IOException {
        try {
            List<Medias> medias = mediasFacade.findByModelAndModelId(model, modelId);
            for (Medias media : medias) {
                fileStorageUtil.deleteFile(media.getDisk());
                mediasFacade.remove(media);
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete media by model and modelId: " + e.getMessage(), e);
        }
    }
} 