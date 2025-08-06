package org.demoproject.producer;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.demoproject.dto.EventStatus;
import org.demoproject.event.SchedulingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class DataProcessingService {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConcurrentMap<Long, ScheduledFuture<?>> eventSchedulers;

    public DataProcessingService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.eventSchedulers = new ConcurrentHashMap<>();
    }

    @EventListener
    public void handleSchedulingEvent(SchedulingEvent event) {
        log.info("Received SchedulingEvent for event ID: {} with status: {}",
                event.getEventId(), event.getEventStatus().getText());

        if (event.getEventStatus() == EventStatus.LIVE) {
            startSchedulerForEvent(event.getEventId());
        } else if (event.getEventStatus() == EventStatus.NOT_LIVE) {
            stopSchedulerForEvent(event.getEventId());
        }

        log.info("Current active schedulers: {}, Event IDs: {}", eventSchedulers.size(), eventSchedulers.keySet());
    }

    private void startSchedulerForEvent(Long eventId) {
        // Use putIfAbsent for atomic check-and-set
        ScheduledFuture<?> existingTask = eventSchedulers.get(eventId);
        if (existingTask != null && !existingTask.isCancelled()) {
            log.info("Scheduler for event ID {} is already running", eventId);
            return;
        }

        ScheduledFuture<?> newTask = taskScheduler.scheduleAtFixedRate(
                () -> processDataForEvent(eventId),
                Duration.ofSeconds(10)
        );

        eventSchedulers.put(eventId, newTask);
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