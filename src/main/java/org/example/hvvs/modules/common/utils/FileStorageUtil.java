package org.example.hvvs.modules.common.utils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@ApplicationScoped
public class FileStorageUtil {

    private final String rootDir = "medias";
    private Path absoluteRootPath;

    @Inject
    private ServletContext servletContext;

    @PostConstruct
    public void init() {
        String realPath = servletContext.getRealPath("/" + rootDir);
        if (realPath == null) {
            throw new RuntimeException("Cannot determine real path for 'medias' directory.");
        }
        this.absoluteRootPath = Paths.get(realPath);
        createRootDirectory();
    }

    public FileStorageUtil() {
        this.absoluteRootPath = Paths.get("/Users/jameswong/IdeaProjects/HVVS/src/main/webapp/medias");
        createRootDirectory();
    }

    private void createRootDirectory() {
        try {
            Files.createDirectories(absoluteRootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create root directory!", e);
        }
    }

    public StorageResult storeFile(Part file, String collection) throws IOException {
        String fileName = generateUniqueFileName(file.getSubmittedFileName());
        String targetCollection = collection != null ? collection : "default";

        Path collectionPath = absoluteRootPath.resolve(targetCollection);
        Path targetLocation = collectionPath.resolve(fileName);

        Files.createDirectories(collectionPath);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Construct the full relative path including the root directory for frontend
            String relativePath = Paths.get("/", rootDir, targetCollection, fileName).toString();
            return new StorageResult(relativePath, targetLocation.toString());
        }
    }


    public void deleteFile(String relativePath) {
        try {
            Path storedPath = Paths.get(relativePath);
            if (storedPath.getNameCount() > 0 && storedPath.getName(0).toString().equals(rootDir)) {
                // Strip the rootDir from the path before resolving
                Path adjustedPath = storedPath.subpath(1, storedPath.getNameCount());
                Path filePath = absoluteRootPath.resolve(adjustedPath);
                Files.deleteIfExists(filePath);
            } else {
                // Fallback: resolve directly (handle unexpected paths)
                Path filePath = absoluteRootPath.resolve(storedPath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + relativePath, e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    public static class StorageResult {
        private final String relativePath;
        private final String absolutePath;

        public StorageResult(String relativePath, String absolutePath) {
            this.relativePath = relativePath;
            this.absolutePath = absolutePath;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }
    }
} 