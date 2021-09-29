package com.finance.loader.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class NotificationDTO {
    private NoticeStatus status;
    private NoticeType type;
    private LocalDate created;
    private LocalDate modified;
    private String createdBy;
    private String modifiedBy;


    public NotificationDTO(NoticeStatus status, NoticeType type, LocalDate created, LocalDate modified) {
        this.status = status;
        this.type = type;
        this.created = created;
        this.modified = modified;
    }

    public enum NoticeType {
        INFO, ERROR
    }

    public enum NoticeStatus {
        NEW, COMPLETED
    }
}
