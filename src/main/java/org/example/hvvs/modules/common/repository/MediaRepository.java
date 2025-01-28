package org.example.hvvs.modules.common.repository;

import org.example.hvvs.model.Medias;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository {
    Medias save(Medias media);

    Optional<Medias> findById(UUID id);

    List<Medias> findByModelAndModelId(String model, String modelId);

    List<Medias> findByCollection(String collection);

    void delete(Medias media);

    void deleteByModelAndModelId(String model, String modelId);
} 