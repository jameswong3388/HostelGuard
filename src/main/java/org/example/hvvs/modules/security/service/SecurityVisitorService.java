package org.example.hvvs.modules.security.service;

import org.example.hvvs.model.VisitRequest;
import org.example.hvvs.model.VisitorRecord;
import org.primefaces.model.file.UploadedFile;

public interface SecurityVisitorService {
    VisitRequest verifyVisitRequest(String verificationCode);

    void registerVisitor(VisitorRecord visitorRecord, UploadedFile visitorPhoto);

    VisitorRecord findVisitorForCheckout(String verificationCode);

    void checkoutVisitor(VisitorRecord visitorRecord);
} 