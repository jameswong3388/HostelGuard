package org.example.hvvs.modules.security.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.commonClasses.CustomPart;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.VisitRequest;
import org.example.hvvs.model.VisitorRecord;
import org.example.hvvs.modules.common.service.MediaService;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.UUID;

@Stateless
public class OnboardVisitorsServiceImpl implements OnboardVisitorsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private MediaService mediaService;

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
    public void registerVisitor(VisitorRecord visitorRecord, UploadedFile tempVisitorPhoto) {
        try {
            // First persist the visitor record to get an ID
            entityManager.persist(visitorRecord);
            entityManager.flush(); // Force the persistence to get the ID

            // Now save visitor photo if provided
            if (tempVisitorPhoto != null && tempVisitorPhoto.getContent() != null) {
                mediaService.deleteByModelAndModelId("visitor-record", visitorRecord.getId().toString());

                InputStream input = tempVisitorPhoto.getInputStream();
                CustomPart part = new CustomPart(
                        tempVisitorPhoto.getFileName(),
                        tempVisitorPhoto.getContentType(),
                        tempVisitorPhoto.getSize(),
                        input
                );

                Medias media = mediaService.uploadFile(part, "visitor-record", visitorRecord.getId().toString(), "visitor-images");
            }

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