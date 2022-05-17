package com.codeofcarbon.accountservice.repository;

import com.codeofcarbon.accountservice.model.EventLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventLog, Long> {

    @Query("select e from EventLog e order by e.id")
    List<EventLog> findAllEventsOrderById();
}
