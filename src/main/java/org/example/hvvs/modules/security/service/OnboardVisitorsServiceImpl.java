package org.example.hvvs.modules.security.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.modules.common.service.NotificationService;
import org.example.hvvs.utils.CustomPart;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitorRecords;
import org.example.hvvs.modules.common.service.MediaService;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

import static org.example.hvvs.model.Notifications.NotificationType.ENTRY_EXIT;
import static org.example.hvvs.model.Notifications.NotificationType.VISIT_REMINDER;

@Stateless
public class OnboardVisitorsServiceImpl implements OnboardVisitorsService {

    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private MediaService mediaService;

    @EJB
    private NotificationService notificationService;

    @Override
    public VisitRequests verifyVisitRequest(String verificationCode) {
        TypedQuery<VisitRequests> query = entityManager.createQuery(
                "SELECT v FROM VisitRequests v WHERE v.verification_code = :code " +
                        "AND v.number_of_entries >= 1 " +
                        "AND (v.status = 'APPROVED' OR v.status = 'PROGRESS')",
                VisitRequests.class
        );
        query.setParameter("code", verificationCode);

        try {
            VisitRequests request = query.getSingleResult();
            request.setStatus(VisitRequests.VisitStatus.PROGRESS);
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
            request.setStatus(VisitRequests.VisitStatus.PROGRESS);
            entityManager.merge(request);

            // send notification to the resident 
            notificationService.createNotification(
                    request.getUserId(),
                    VISIT_REMINDER,
                    "New visitor Check-in",
                    "A new visitor " + visitorRecord.getVisitorName() + " has checked in, please verify the visitor",
                    "visit-requests",
                    request.getId().toString()
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to save visitor photo", e);
        }
    }

    @Override
    public List<VisitorRecords> findVisitorsForCheckout(String code) {
        return entityManager.createQuery(
                        "SELECT vr FROM VisitorRecords vr " +
                                "JOIN vr.request_id r " +
                                "WHERE r.verification_code = :code " +
                                "AND r.status = 'PROGRESS' " +
                                "AND vr.check_out_time IS NULL",
                        VisitorRecords.class)
                .setParameter("code", code)
                .getResultList();
    }

    @Override
    public void checkoutVisitor(VisitorRecords visitorRecord) {
        // Set checkout time
        visitorRecord.setCheckOutTime(new Timestamp(System.currentTimeMillis()));

        // Update visitor record
        entityManager.merge(visitorRecord);

        // Check remaining visitors without checkout
        VisitRequests request = visitorRecord.getRequestId();
        Long remainingVisitors = entityManager.createQuery(
                        "SELECT COUNT(v) FROM VisitorRecords v WHERE v.request_id = :request AND v.check_out_time IS NULL",
                        Long.class)
                .setParameter("request", request)
                .getSingleResult();

        if (remainingVisitors == 0) {
            request.setStatus(VisitRequests.VisitStatus.COMPLETED);
            entityManager.merge(request);

            // send notification to the resident 
            notificationService.createNotification(
                    request.getUserId(),
                    ENTRY_EXIT,
                    "Visitor Check-out",
                    "All visitors have checked out, please verify the visitor",
                    "visit-requests",
                    request.getId().toString()
            );
        } else {
            // send notification to the resident 
            notificationService.createNotification(
                    request.getUserId(),
                    ENTRY_EXIT,
                    "Visitor Check-out",
                    "Visitor " + visitorRecord.getVisitorName() + " has checked out, please verify the visitor",
                    "visit-requests",
                    request.getId().toString()
            );
        }
    }
} 