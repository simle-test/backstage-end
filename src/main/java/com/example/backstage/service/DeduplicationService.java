package com.example.backstage.service;

import com.example.backstage.dto.DeduplicationRequest;
import com.example.backstage.dto.DeduplicationResult;
import com.example.backstage.dto.DuplicateDetail;

import java.util.List;

public interface DeduplicationService {
    
    DeduplicationResult detectDuplicates(DeduplicationRequest request);
    
    DeduplicationResult removeDuplicates(DeduplicationRequest request);
    
    DeduplicationResult previewDeduplication(DeduplicationRequest request);
    
    List<DuplicateDetail> findDuplicatesByField(String fieldName, String fieldValue);
    
    long countDuplicates();
}