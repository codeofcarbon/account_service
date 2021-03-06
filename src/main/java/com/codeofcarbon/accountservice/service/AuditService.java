package com.codeofcarbon.accountservice.service;

import com.codeofcarbon.accountservice.model.*;
import com.codeofcarbon.accountservice.model.dto.EventLogDTO;
import com.codeofcarbon.accountservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {
    private final EventRepository eventRepository;

    public List<EventLogDTO> getSecurityEvents() {
        return eventRepository.findAllEventsOrderById().stream()
                .map(EventLogDTO::mapToEventDTO)
                .collect(Collectors.toList());
    }

    public void logEvent(Action action, String subject, String object, String path) {
        eventRepository.save(EventLog.builder()
                .date(LocalDateTime.now())
                .action(action)
                .subject(subject)
                .object(object)
                .path(path)
                .build());
    }
}