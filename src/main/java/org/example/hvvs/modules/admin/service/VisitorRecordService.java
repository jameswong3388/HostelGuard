package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.VisitorRecords;

import java.util.List;

public interface VisitorRecordService {
    List<VisitorRecords> getAllVisitorRecords();
    VisitorRecords getVisitorRecordById(Long id);
    void createVisitorRecord(VisitorRecords record);
    void updateVisitorRecord(VisitorRecords record);
    void deleteVisitorRecord(VisitorRecords record);
} 