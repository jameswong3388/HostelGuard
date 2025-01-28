package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.model.Medias;
import org.example.hvvs.modules.admin.service.DashboardService;
import org.example.hvvs.modules.common.service.MediaService;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class MediaManagementController implements Serializable {

    @Inject
    private DashboardService dashboardService;

    @Inject
    private MediaService mediaService;

    private UploadedFile file;
    private String selectedCollection;
    private LineChartModel visitorActivityChart;
    private Medias selectedMedia;
    private List<Medias> profilePictureMedia;
    private List<Medias> documentsMedia;
    private List<Medias> reportsMedia;
    private List<Medias> othersMedia;

    @PostConstruct
    public void init() {
        loadAllMedia();
    }

    private void loadAllMedia() {
        profilePictureMedia = mediaService.findByCollection("profile-pictures");
        documentsMedia = mediaService.findByCollection("documents");
        reportsMedia = mediaService.findByCollection("reports");
        othersMedia = mediaService.findByCollection("others");
    }

    public void deleteMedia(UUID id) {
        try {
            mediaService.deleteMedia(id);
            loadAllMedia(); // Refresh lists
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Media deleted successfully");
        } catch (IOException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete media: " + e.getMessage());
        }
    }

    public void upload() {
        if (file == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select a file to upload");
            return;
        }

        try (InputStream input = file.getInputStream()) {
            CustomPart part = new CustomPart(
                file.getFileName(),
                file.getContentType(),
                file.getSize(),
                input
            );

            Medias media = mediaService.uploadFile(
                part,
                "user", // model name
                "1",    // model ID
                selectedCollection
            );

            loadAllMedia();
            addMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "File uploaded successfully: " + media.getFileName());

        } catch (IOException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Failed to upload file: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(severity, summary, detail));
    }

    public String formatFileSize(Double bytes) {
        if (bytes == null) return "0 B";
        
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    // Getters and Setters
    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getSelectedCollection() {
        return selectedCollection;
    }

    public void setSelectedCollection(String selectedCollection) {
        this.selectedCollection = selectedCollection;
    }


    public Medias getSelectedMedia() {
        return selectedMedia;
    }

    public void setSelectedMedia(Medias selectedMedia) {
        this.selectedMedia = selectedMedia;
    }

    public List<Medias> getProfilePictureMedia() {
        return profilePictureMedia;
    }

    public List<Medias> getDocumentsMedia() {
        return documentsMedia;
    }

    public List<Medias> getReportsMedia() {
        return reportsMedia;
    }

    public List<Medias> getOthersMedia() {
        return othersMedia;
    }

    // Custom Part implementation to bridge UploadedFile to Part
    private static class CustomPart implements jakarta.servlet.http.Part {
        private final String fileName;
        private final String contentType;
        private final long size;
        private final InputStream content;

        public CustomPart(String fileName, String contentType, long size, InputStream content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.size = size;
            this.content = content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return content;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getSubmittedFileName() {
            return fileName;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public void write(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public java.util.Collection<String> getHeaders(String name) {
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.Collection<String> getHeaderNames() {
            return java.util.Collections.emptyList();
        }
    }
} 