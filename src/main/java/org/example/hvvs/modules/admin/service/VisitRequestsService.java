package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.VisitRequest;

import java.util.List;

public interface VisitRequestsService {
    List<VisitRequest> getAllRequests();

    void updateRequest(VisitRequest request);

    void deleteRequests(List<VisitRequest> requests);
} 