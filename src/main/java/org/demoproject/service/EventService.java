package org.demoproject.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.demoproject.dto.Event;
import org.demoproject.dto.EventStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class EventService {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConcurrentMap<Long, ScheduledFuture<?>> eventSchedulers;

    public EventService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.eventSchedulers = new ConcurrentHashMap<>();
    }

    public void addEvent(Event event) {
        log.info("Received SchedulingEvent for event ID: {} with status: {}",
                event.getEventId(), event.getStatus().getText());

        if (event.getStatus() == EventStatus.LIVE) {
            startSchedulerForEvent(event.getEventId());
        } else if (event.getStatus() == EventStatus.NOT_LIVE) {
            stopSchedulerForEvent(event.getEventId());
        }

        log.info("Current active schedulers: {}, Event IDs: {}", eventSchedulers.size(), eventSchedulers.keySet());
    }

    private void startSchedulerForEvent(Long eventId) {
        // TODO Use putIfAbsent for atomic check-and-set
        ScheduledFuture<?> existingTask = eventSchedulers.get(eventId);
        if (existingTask != null && !existingTask.isCancelled()) {
            log.info("Scheduler for event ID {} is already running", eventId);
            return;
        }

        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(
                () -> processDataForEvent(eventId),
                Duration.ofSeconds(10)
        );

        eventSchedulers.put(eventId, task);
        log.info("Scheduler started for event ID: {}. Total active: {}",
                eventId, eventSchedulers.size());
    }

    private void stopSchedulerForEvent(Long eventId) {
        ScheduledFuture<?> task = eventSchedulers.remove(eventId);

        if (task != null && !task.isCancelled()) {
            task.cancel(false);
            log.info("Scheduler stopped for event ID: {}. Total active: {}",
                    eventId, eventSchedulers.size());
        }
    }

    private void processDataForEvent(Long eventId) {
        String threadName = Thread.currentThread().getName();
        long threadId = Thread.currentThread().threadId();

        log.info("Processing data for event ID: {} on thread: {} (ID: {})",
                eventId, threadName, threadId);
    }

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