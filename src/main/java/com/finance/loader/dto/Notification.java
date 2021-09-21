package com.finance.loader.dto;

import java.time.LocalDate;

public class Notification {
    private NoticeStatus status;
    private NoticeType type;
    private LocalDate created;
    private LocalDate modified;
    private String createdBy;
    private String modifiedBy;

    public Notification() {
    }

    public Notification(NoticeStatus status, NoticeType type, LocalDate created, LocalDate modified) {
        this.status = status;
        this.type = type;
        this.created = created;
        this.modified = modified;
    }

    public NoticeStatus getStatus() {
        return status;
    }

    public void setStatus(NoticeStatus status) {
        this.status = status;
    }

    public NoticeType getType() {
        return type;
    }

    public void setType(NoticeType type) {
        this.type = type;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public LocalDate getModified() {
        return modified;
    }

    public void setModified(LocalDate modified) {
        this.modified = modified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public enum NoticeType {
        INFO, ERROR
    }

    public enum NoticeStatus {
        NEW, COMPLETED
    }
}
