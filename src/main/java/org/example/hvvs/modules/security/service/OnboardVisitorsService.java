package org.example.hvvs.modules.security.service;

import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitorRecords;
import org.primefaces.model.file.UploadedFile;

import java.util.List;

public interface OnboardVisitorsService {
    VisitRequests verifyVisitRequest(String verificationCode);

    void registerVisitor(VisitorRecords visitorRecord, UploadedFile visitorPhoto);

    List<VisitorRecords> findVisitorsForCheckout(String verificationCode);

    void checkoutVisitor(VisitorRecords visitorRecord);
} 