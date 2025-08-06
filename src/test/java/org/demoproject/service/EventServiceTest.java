package org.demoproject.service;

import org.demoproject.model.Event;
import org.demoproject.model.EventStatus;
import org.demoproject.model.ExternalApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// OK
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private ScorePublisherService scorePublisherService;

    @Mock
    private ExternalApiService externalApiService;

    @InjectMocks
    private EventService eventService;

    @Test
    void testThatServicesAreCalledWithSameEventId() {
        final Long eventId = 123L;
        final String scores = "1:1";
        final Event liveEvent = new Event(eventId, EventStatus.LIVE);
        final ScheduledFuture<?> mockScheduledFuture = mock(ScheduledFuture.class);
        final ExternalApiResponse mockApiResponse = mock(ExternalApiResponse.class);
        when(mockApiResponse.currentScore()).thenReturn(scores);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return mockScheduledFuture;
        }).when(taskScheduler).scheduleAtFixedRate(any(Runnable.class), any(Duration.class));

        doReturn(mockApiResponse)
                .when(externalApiService)
                .fetchEventScores(eq(eventId));

        eventService.addEvent(liveEvent);

        verify(externalApiService).fetchEventScores(eq(eventId));
    }

    @Test
    void testThatServicesAreNeverCalledWithNotLiveEvent() {
        final Long eventId = 123L;
        final Event notLiveEvent = new Event(eventId, EventStatus.NOT_LIVE);

        eventService.addEvent(notLiveEvent);

        verify(taskScheduler, never())
                .scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
        verify(externalApiService, never()).fetchEventScores(any(Long.class));
        verify(scorePublisherService, never()).publishEventResult(eq(eventId), any());
    }

    @Test
    void testThatEventIsCancelledCorrectly() {
        final Long eventId = 123L;
        final Event liveEvent = new Event(eventId, EventStatus.LIVE);
        final Event notLiveEvent = new Event(eventId, EventStatus.NOT_LIVE);
        final ScheduledFuture<?> mockScheduledFuture = mock(ScheduledFuture.class);

        doReturn(mockScheduledFuture)
                .when(taskScheduler)
                .scheduleAtFixedRate(any(Runnable.class), any(Duration.class));

        eventService.addEvent(liveEvent);
        eventService.addEvent(notLiveEvent);

        verify(mockScheduledFuture).cancel(false);
    }


}
