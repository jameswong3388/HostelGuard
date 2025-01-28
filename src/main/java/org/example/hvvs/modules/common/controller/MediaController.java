package org.example.hvvs.modules.common.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.hvvs.model.Medias;
import org.example.hvvs.modules.common.service.MediaService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Path("/api/media")
@RequestScoped
public class MediaController {

    @Inject
    private MediaService mediaService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @FormParam("file") Part file,
            @FormParam("model") String model,
            @FormParam("modelId") String modelId,
            @FormParam("collection") String collection) {
        try {
            Medias media = mediaService.uploadFile(file, model, modelId, collection);
            return Response.ok(media).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to upload file: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedia(@PathParam("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return mediaService.findById(uuid)
                    .map(media -> Response.ok(media).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid UUID format")
                    .build();
        }
    }

    @GET
    @Path("/model/{model}/{modelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMediaByModel(
            @PathParam("model") String model,
            @PathParam("modelId") String modelId) {
        List<Medias> medias = mediaService.findByModelAndModelId(model, modelId);
        return Response.ok(medias).build();
    }

    @GET
    @Path("/collection/{collection}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMediaByCollection(@PathParam("collection") String collection) {
        List<Medias> medias = mediaService.findByCollection(collection);
        return Response.ok(medias).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMedia(@PathParam("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            mediaService.deleteMedia(uuid);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid UUID format")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete media: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/model/{model}/{modelId}")
    public Response deleteMediaByModel(
            @PathParam("model") String model,
            @PathParam("modelId") String modelId) {
        try {
            mediaService.deleteByModelAndModelId(model, modelId);
            return Response.ok().build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete media: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/file/{id}")
    public Response getMediaFile(@PathParam("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return mediaService.findById(uuid)
                    .map(media -> {
                        try {
                            java.nio.file.Path filePath = java.nio.file.Paths.get(media.getDisk());
                            String mimeType = media.getMimeType() != null ? media.getMimeType() : "application/octet-stream";
                            
                            return Response.ok(java.nio.file.Files.newInputStream(filePath))
                                    .header("Content-Type", mimeType)
                                    .header("Content-Disposition", "inline; filename=\"" + media.getFileName() + "\"")
                                    .build();
                        } catch (IOException e) {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity("Failed to read file: " + e.getMessage())
                                    .build();
                        }
                    })
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid UUID format")
                    .build();
        }
    }
} 