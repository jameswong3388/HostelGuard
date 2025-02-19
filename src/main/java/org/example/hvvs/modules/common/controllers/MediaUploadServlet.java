package org.example.hvvs.modules.common.controllers;


import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.example.hvvs.model.Medias;
import org.example.hvvs.modules.common.service.MediaService;

import java.io.IOException;

@WebServlet("/api/media/upload")
@MultipartConfig
public class MediaUploadServlet extends HttpServlet {
    
    @EJB
    private MediaService mediaService;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
        try {
            Part filePart = request.getPart("file");
            String model = request.getParameter("model");
            String modelId = request.getParameter("modelId");
            String collection = request.getParameter("collection");

            Medias media = mediaService.uploadFile(filePart, model, modelId, collection);
            
            response.setContentType("application/json");
            response.getWriter().print(
                String.format("{\"id\":\"%s\",\"url\":\"%s\",\"fileName\":\"%s\",\"size\":%f}",
                    media.getId(), media.getDisk(), media.getFileName(), media.getSize())
            );
            
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Upload failed: " + e.getMessage());
        }
    }
}
