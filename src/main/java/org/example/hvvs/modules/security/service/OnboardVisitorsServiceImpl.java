package org.example.hvvs.modules.security.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.commonClasses.CustomPart;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.modules.common.service.MediaService;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

@Stateless
public class OnboardVisitorsServiceImpl implements OnboardVisitorsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private MediaService mediaService;

    @Override
    public VisitRequests verifyVisitRequest(String verificationCode) {
        TypedQuery<VisitRequests> query = entityManager.createQuery(
                "SELECT v FROM VisitRequests v WHERE v.verification_code = :code AND v.status = 'APPROVED'",
                VisitRequests.class
        );
        query.setParameter("code", verificationCode);

        try {
            VisitRequests request = query.getSingleResult();
            request.setStatus("PROGRESS");
            entityManager.merge(request);
            return request;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void registerVisitor(VisitorRecords visitorRecord, UploadedFile tempVisitorPhoto) {
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
            VisitRequests request = visitorRecord.getRequestId();
            request.setStatus("PROGRESS");
            entityManager.merge(request);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save visitor photo", e);
        }
    }

    @Override
    public VisitorRecords findVisitorForCheckout(String verificationCode) {
        TypedQuery<VisitorRecords> query = entityManager.createQuery(
            "SELECT vr FROM VisitorRecords vr " +
            "JOIN vr.request_id r " +
            "WHERE r.verification_code = :code " +
            "AND r.status = 'PROGRESS' " +
            "AND vr.check_out_time IS NULL",
            VisitorRecords.class
        );
        query.setParameter("code", verificationCode);
        
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void checkoutVisitor(VisitorRecords visitorRecord) {
        // Set checkout time
        visitorRecord.setCheckOutTime(new Timestamp(System.currentTimeMillis()));
        visitorRecord.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Update visitor record
        entityManager.merge(visitorRecord);

        // Update visit request status
        VisitRequests request = visitorRecord.getRequestId();
        request.setStatus("COMPLETED");
        entityManager.merge(request);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
} 