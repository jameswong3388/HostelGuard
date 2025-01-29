package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.commonClasses.CustomPart;
import org.example.hvvs.model.Medias;
import org.example.hvvs.modules.common.service.MediaService;
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
    private MediaService mediaService;

    private UploadedFile file;
    private String selectedCollection;
    private String model;
    private String modelId;
    private Medias selectedMedia;
    private List<Medias> profilePictureMedia;
    private List<Medias> documentsMedia;
    private List<Medias> reportsMedia;
    private List<Medias> visitorImagesMedia;
    private List<Medias> othersMedia;
    private List<String> availableModels;
    private Medias selectedPreviewMedia;

    @PostConstruct
    public void init() {
        loadAllMedia();
        // Initialize available models
        availableModels = List.of(
                "visitor_records",
                "visit_requests",
                "security_staff_profiles",
                "resident_profiles",
                "managing_staff_profiles",
                "users"
        );
    }

    private void loadAllMedia() {
        profilePictureMedia = mediaService.findByCollection("profile-pictures");
        documentsMedia = mediaService.findByCollection("documents");
        reportsMedia = mediaService.findByCollection("reports");
        visitorImagesMedia = mediaService.findByCollection("visitor-images");
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
                    model,
                    modelId,
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
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

    public List<Medias> getVisitorImagesMedia() {
        return visitorImagesMedia;
    }

    public List<Medias> getOthersMedia() {
        return othersMedia;
    }

    public List<String> getAvailableModels() {
        return availableModels;
    }

    // Getter and Setter
    public Medias getSelectedPreviewMedia() {
        return selectedPreviewMedia;
    }

    public void setSelectedPreviewMedia(Medias selectedPreviewMedia) {
        this.selectedPreviewMedia = selectedPreviewMedia;
    }

    public void previewMedia(Medias media) {
        this.selectedPreviewMedia = media;
    }
} 