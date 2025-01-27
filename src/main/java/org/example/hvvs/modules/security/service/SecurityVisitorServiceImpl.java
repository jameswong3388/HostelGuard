package org.example.hvvs.modules.security.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.VisitRequest;
import org.example.hvvs.model.VisitorRecord;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.UUID;

@Stateless
public class SecurityVisitorServiceImpl implements SecurityVisitorService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public VisitRequest verifyVisitRequest(String verificationCode) {
        TypedQuery<VisitRequest> query = entityManager.createQuery(
                "SELECT v FROM VisitRequest v WHERE v.verification_code = :code AND v.status = 'APPROVED'",
                VisitRequest.class
        );
        query.setParameter("code", verificationCode);

        try {
            VisitRequest request = query.getSingleResult();
            request.setStatus("PROGRESS");
            entityManager.merge(request);
            return request;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void registerVisitor(VisitorRecord visitorRecord, UploadedFile visitorPhoto) {
        try {
            // Save visitor photo
            if (visitorPhoto != null && visitorPhoto.getContent() != null) {
                String fileName = UUID.randomUUID().toString() + getFileExtension(visitorPhoto.getFileName());
                Path uploadPath = Paths.get(System.getProperty("jboss.server.data.dir"), "visitor-photos");
                Files.createDirectories(uploadPath);

                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, visitorPhoto.getContent());

                // Store the photo path in the database
                // You might want to add a photo_path field to VisitorRecord
            }

            // Save visitor record
            entityManager.persist(visitorRecord);

            // Update visit request status
            VisitRequest request = visitorRecord.getRequestId();
            request.setStatus("PROGRESS");
            entityManager.merge(request);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save visitor photo", e);
        }
    }

    @Override
    public VisitorRecord findVisitorForCheckout(String verificationCode) {
        TypedQuery<VisitorRecord> query = entityManager.createQuery(
            "SELECT vr FROM VisitorRecord vr " +
            "JOIN vr.request_id r " +
            "WHERE r.verification_code = :code " +
            "AND r.status = 'PROGRESS' " +
            "AND vr.check_out_time IS NULL",
            VisitorRecord.class
        );
        query.setParameter("code", verificationCode);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void checkoutVisitor(VisitorRecord visitorRecord) {
        // Set checkout time
        visitorRecord.setCheckOutTime(new Timestamp(System.currentTimeMillis()));
        visitorRecord.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Update visitor record
        entityManager.merge(visitorRecord);

        // Update visit request status
        VisitRequest request = visitorRecord.getRequestId();
        request.setStatus("COMPLETED");
        entityManager.merge(request);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
} 