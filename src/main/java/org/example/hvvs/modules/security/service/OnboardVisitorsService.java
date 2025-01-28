package org.example.hvvs.modules.security.service;

import org.example.hvvs.model.VisitRequests;
import org.example.hvvs.model.VisitorRecords;
import org.primefaces.model.file.UploadedFile;

public interface OnboardVisitorsService {
    VisitRequests verifyVisitRequest(String verificationCode);

    void registerVisitor(VisitorRecords visitorRecord, UploadedFile visitorPhoto);

    VisitorRecords findVisitorForCheckout(String verificationCode);

    void checkoutVisitor(VisitorRecords visitorRecord);
} 