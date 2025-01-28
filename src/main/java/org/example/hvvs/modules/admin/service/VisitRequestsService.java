package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.VisitRequests;

import java.util.List;

public interface VisitRequestsService {
    List<VisitRequests> getAllRequests();

    void updateRequest(VisitRequests request);

    void deleteRequests(List<VisitRequests> requests);
} 