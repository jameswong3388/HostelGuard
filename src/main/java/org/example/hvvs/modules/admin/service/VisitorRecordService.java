package org.example.hvvs.modules.admin.service;

import org.example.hvvs.model.VisitorRecord;
import java.util.List;

public interface VisitorRecordService {
    List<VisitorRecord> getAllVisitorRecords();
    VisitorRecord getVisitorRecordById(Long id);
    void createVisitorRecord(VisitorRecord record);
    void updateVisitorRecord(VisitorRecord record);
    void deleteVisitorRecord(VisitorRecord record);
} 