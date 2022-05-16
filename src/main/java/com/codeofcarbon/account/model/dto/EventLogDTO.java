package com.codeofcarbon.account.model.dto;

import com.codeofcarbon.account.model.EventLog;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@ToString
public class EventLogDTO {
    private final long id;
    private final String date;
    private final String action;
    private final String subject;
    private final String object;
    private final String path;

    public static EventLogDTO mapToEventDTO(EventLog eventLog) {
        return EventLogDTO.builder()
                .id(eventLog.getId())
                .date(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(eventLog.getDate()))
                .action(eventLog.getAction().name())
                .subject(eventLog.getSubject())
                .object(eventLog.getObject())
                .path(eventLog.getPath())
                .build();
    }
}