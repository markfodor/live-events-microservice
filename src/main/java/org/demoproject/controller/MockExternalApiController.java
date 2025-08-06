package org.demoproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.demoproject.model.MockEventScoreResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/mock")
public class MockExternalApiController {

    private final Random random = new Random();

    @GetMapping("/events/{eventId}")
    public ResponseEntity<MockEventScoreResponse> getEventScore(@PathVariable String eventId) {
        int homeScore = random.nextInt(6);
        int awayScore = random.nextInt(6);
        String currentScore = homeScore + ":" + awayScore;

        MockEventScoreResponse response = new MockEventScoreResponse(eventId, currentScore);
        return ResponseEntity.ok(response);
    }
}

