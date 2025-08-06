package org.demoproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.demoproject.model.Event;
import org.demoproject.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(final EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createNote(@Valid @RequestBody Event event) {
        log.info("Event arrived with '{}' id and status '{}'", event.eventId(), event.status().getText());
        eventService.addEvent(event);
    }
}

