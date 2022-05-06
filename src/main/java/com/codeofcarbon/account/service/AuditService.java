package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.Action;
import com.codeofcarbon.account.model.EventLog;
import com.codeofcarbon.account.model.dto.EventLogDTO;
import com.codeofcarbon.account.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {
    private final EventRepository eventRepository;

    public List<EventLogDTO> getSecurityEvents() {
      return eventRepository.findAll().stream()
                .map(EventLogDTO::mapToEventDTO)
                .sorted(Comparator.comparingLong(EventLogDTO::getId))
                .collect(Collectors.toList());
    }

    public void logEvent(Action action, @Nullable String subject, String object, String path) {
        var eventLog = new EventLog();
        eventLog.setDate(LocalDateTime.now());
        eventLog.setAction(action);
        eventLog.setSubject(subject);
        eventLog.setObject(object);
        eventLog.setPath(path);
        eventRepository.save(eventLog);
    }
}