package com.futureschole.eventpipeline.application;

import com.futureschole.eventpipeline.domain.EventLog;
import com.futureschole.eventpipeline.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void save(EventLog eventLog) {
        eventRepository.save(eventLog);
    }
}
