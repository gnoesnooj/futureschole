package com.futureschole.eventpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class EventLog {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID logId;

    @Column(nullable = false)
    private ZonedDateTime eventTime;

    @Column(nullable = false, length = 50)
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    @Column(length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviceType deviceType;

    @Builder
    public EventLog(UUID logId, ZonedDateTime eventTime, UUID userId, String sessionId,
                    EventType eventType, String ipAddress, DeviceType deviceType) {
        this.logId = logId != null ? logId : UUID.randomUUID();
        this.eventTime = eventTime != null ? eventTime : ZonedDateTime.now();
        this.userId = userId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.deviceType = deviceType;
    }
}
