package com.futureschole.eventpipeline.domain.repository;

import com.futureschole.eventpipeline.domain.EventLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventLog, UUID> {

    EventLog save(EventLog eventLog);
}
