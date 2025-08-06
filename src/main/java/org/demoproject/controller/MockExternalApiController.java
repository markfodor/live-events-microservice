package org.demoproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.demoproject.model.Event;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/mock")
public class MockExternalApiController {

    private final Random random = new Random();

    @GetMapping("/events/{eventId}/score")
    public ResponseEntity<EventScoreResponse> getEventScore(@PathVariable String eventId) {
        int homeScore = random.nextInt(6); // 0-5 goals
        int awayScore = random.nextInt(6); // 0-5 goals
        String currentScore = homeScore + ":" + awayScore;

        EventScoreResponse response = new EventScoreResponse(eventId, currentScore);

        log.info("Returning score for event {}: {}", eventId, currentScore);
        return ResponseEntity.ok(response);
    }

    public record EventScoreResponse(String eventId, String currentScore) {}
}

