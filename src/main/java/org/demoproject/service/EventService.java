package org.demoproject.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.demoproject.model.Event;
import org.demoproject.model.EventStatus;
import org.demoproject.model.ExternalApiResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class EventService {
    public static final int SCHEDULED_TASK_PERIOD_IN_SECONDS = 10;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConcurrentMap<Long, ScheduledFuture<?>> eventSchedulers;
    private final ExternalApiService externalApiService;
    private final ScorePublisherService scorePublisherService;

    public EventService(final ThreadPoolTaskScheduler taskScheduler, final ExternalApiService externalApiService,
                        final ScorePublisherService scorePublisherService) {
        this.taskScheduler = taskScheduler;
        this.externalApiService = externalApiService;
        this.scorePublisherService = scorePublisherService;
        this.eventSchedulers = new ConcurrentHashMap<>();
    }

    public void addEvent(final Event event) {
        log.info("Received SchedulingEvent for event ID: {} with status: {}",
                event.eventId(), event.status().getText());

        if (event.status() == EventStatus.LIVE) {
            startSchedulerForEvent(event.eventId());
        } else if (event.status() == EventStatus.NOT_LIVE) {
            stopSchedulerForEvent(event.eventId());
        }

        log.info("Current active schedulers: {}, Event IDs: {}", eventSchedulers.size(), eventSchedulers.keySet());
    }

    private void startSchedulerForEvent(final Long eventId) {
        ScheduledFuture<?> existingTask = eventSchedulers.get(eventId);
        if (existingTask != null && !existingTask.isCancelled()) {
            log.info("Scheduler for event ID {} is already running", eventId);
            return;
        }

        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> handleEvent(eventId),
                Duration.ofSeconds(SCHEDULED_TASK_PERIOD_IN_SECONDS)
        );

        eventSchedulers.put(eventId, task);
        log.info("Scheduler started for event ID: {}. Total active: {}",
                eventId, eventSchedulers.size());
    }

    private void stopSchedulerForEvent(final Long eventId) {
        ScheduledFuture<?> task = eventSchedulers.remove(eventId);

        if (task != null && !task.isCancelled()) {
            task.cancel(false);
            log.info("Scheduler stopped for event ID: {}. Total active: {}",
                    eventId, eventSchedulers.size());
        }
    }

    private void handleEvent(final Long eventId) {
        String threadName = Thread.currentThread().getName();
        log.info("Processing data for event ID: {} on thread: {}",
                eventId, threadName);

        final ExternalApiResponse response = fetchExternalApiResponse(eventId);
        if (response != null) {
            scorePublisherService.publishEventResult(response.eventId(), response.currentScore());
        }
    }

    private ExternalApiResponse fetchExternalApiResponse(final Long eventId) {
        ExternalApiResponse response = null;

        try {
            response = externalApiService.fetchEventScores(eventId);
        } catch (final Exception exception) { // can be improved later
            log.error("Error occurred while fetching score data.", exception);
        }

        return response;
    }

    // graceful shutdown
    @PreDestroy
    public void shutdown() {
        eventSchedulers.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel(false);
            }
        });
        eventSchedulers.clear();

        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }
}