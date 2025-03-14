package org.example.hvvs.modules.security.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.Notifications;
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
import java.time.LocalDate;
import java.sql.Date;
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
        // Get current date to check if visit day is valid
        Date today = new Date(System.currentTimeMillis());
        
        TypedQuery<VisitRequests> query = entityManager.createQuery(
                "SELECT v FROM VisitRequests v WHERE v.verification_code = :code " +
                        "AND v.visit_day = :today " +
                        "AND (v.status = 'PENDING')",
                VisitRequests.class
        );
        query.setParameter("code", verificationCode);
        query.setParameter("today", today);

        try {
            return query.getSingleResult();
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
            Medias visitorPhotoMedia = null;
            if (tempVisitorPhoto != null && tempVisitorPhoto.getContent() != null) {
                mediaService.deleteByModelAndModelId("visitor-record", visitorRecord.getId().toString());

                InputStream input = tempVisitorPhoto.getInputStream();
                CustomPart part = new CustomPart(
                        tempVisitorPhoto.getFileName(),
                        tempVisitorPhoto.getContentType(),
                        tempVisitorPhoto.getSize(),
                        input
                );

                visitorPhotoMedia = mediaService.uploadFile(part, "visitor-record", visitorRecord.getId().toString(), "visitor-images");
            }

            // Update visit request status
            VisitRequests request = visitorRecord.getRequestId();
            request.setStatus(VisitRequests.VisitStatus.PROGRESS);
            entityManager.merge(request);

            Notifications notification = notificationService.createNotification(
                    request.getUserId(),
                    VISIT_REMINDER,
                    "New visitor Check-in",
                    "Visitor " + request.getVisitorName() + " has checked in to visit your unit",
                    "visit-requests",
                    request.getId().toString()
            );

            // send notification to the resident with the visitor photo attached
            if (visitorPhotoMedia != null && visitorPhotoMedia.getId() != null) {
                try {
                    InputStream input = tempVisitorPhoto.getInputStream();
                    CustomPart part = new CustomPart(
                            tempVisitorPhoto.getFileName(),
                            tempVisitorPhoto.getContentType(),
                            tempVisitorPhoto.getSize(),
                            input
                    );

                    mediaService.uploadFile(
                            part,
                            "notifications",
                            notification.getId().toString(),
                            "notification-media"
                    );
                } catch (IOException e) {
                    // Log the error but continue - notification will be created without media
                    System.err.println("Failed to attach media to notification: " + e.getMessage());
                }

            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save visitor photo", e);
        }
    }

    @Override
    public List<VisitorRecords> findVisitorsForCheckout(String code) {
        TypedQuery<VisitorRecords> query = entityManager.createQuery(
                "SELECT v FROM VisitorRecords v " +
                        "JOIN v.request_id r " +
                        "WHERE r.verification_code = :code " +
                        "AND v.check_out_time IS NULL",
                VisitorRecords.class
        );

        query.setParameter("code", code);
        return query.getResultList();
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
                    "Visitor " + request.getVisitorName() + " has completed their visit",
                    "visit-requests",
                    request.getId().toString()
            );
        } else {
            // send notification to the resident 
            notificationService.createNotification(
                    request.getUserId(),
                    ENTRY_EXIT,
                    "Visitor Check-out",
                    "Visitor from your unit has checked out",
                    "visit-requests",
                    request.getId().toString()
            );
        }
    }
} 