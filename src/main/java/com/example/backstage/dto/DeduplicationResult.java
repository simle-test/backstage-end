package com.example.backstage.dto;

import java.util.List;

public class DeduplicationResult {
    
    private Integer totalCount;
    
    private Integer uniqueCount;
    
    private Integer duplicateCount;
    
    private Integer removedCount;
    
    private Double removalRate;
    
    private String mode;
    
    private List<String> fieldsUsed;
    
    private List<DuplicateGroup> duplicateGroups;
    
    private List<DuplicateDetail> duplicates;
    
    private Long processingTimeMs;
    
    private String message;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getUniqueCount() {
        return uniqueCount;
    }

    public void setUniqueCount(Integer uniqueCount) {
        this.uniqueCount = uniqueCount;
    }

    public Integer getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(Integer duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public Integer getRemovedCount() {
        return removedCount;
    }

    public void setRemovedCount(Integer removedCount) {
        this.removedCount = removedCount;
    }

    public Double getRemovalRate() {
        return removalRate;
    }

    public void setRemovalRate(Double removalRate) {
        this.removalRate = removalRate;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<String> getFieldsUsed() {
        return fieldsUsed;
    }

    public void setFieldsUsed(List<String> fieldsUsed) {
        this.fieldsUsed = fieldsUsed;
    }

    public List<DuplicateGroup> getDuplicateGroups() {
        return duplicateGroups;
    }

    public void setDuplicateGroups(List<DuplicateGroup> duplicateGroups) {
        this.duplicateGroups = duplicateGroups;
    }

    public List<DuplicateDetail> getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(List<DuplicateDetail> duplicates) {
        this.duplicates = duplicates;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DuplicateGroup {
        private Integer representativeId;
        private String representativeTitle;
        private List<DuplicateDetail> duplicates;
        private Integer groupSize;

        public Integer getRepresentativeId() {
            return representativeId;
        }

        public void setRepresentativeId(Integer representativeId) {
            this.representativeId = representativeId;
        }

        public String getRepresentativeTitle() {
            return representativeTitle;
        }

        public void setRepresentativeTitle(String representativeTitle) {
            this.representativeTitle = representativeTitle;
        }

        public List<DuplicateDetail> getDuplicates() {
            return duplicates;
        }

        public void setDuplicates(List<DuplicateDetail> duplicates) {
            this.duplicates = duplicates;
        }

        public Integer getGroupSize() {
            return groupSize;
        }

        public void setGroupSize(Integer groupSize) {
            this.groupSize = groupSize;
        }
    }
}