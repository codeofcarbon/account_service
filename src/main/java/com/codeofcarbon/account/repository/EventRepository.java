package com.codeofcarbon.account.repository;

import com.codeofcarbon.account.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findAll();
}
