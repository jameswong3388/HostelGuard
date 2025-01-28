package org.example.hvvs.modules.common.service;

import jakarta.servlet.http.Part;
import org.example.hvvs.model.Medias;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaService {
    Medias uploadFile(Part file, String model, String modelId, String collection) throws IOException;
    
    Optional<Medias> findById(UUID id);

    List<Medias> findByModelAndModelId(String model, String modelId);

    List<Medias> findByCollection(String collection);

    void deleteMedia(UUID id) throws IOException;

    void deleteByModelAndModelId(String model, String modelId) throws IOException;
}