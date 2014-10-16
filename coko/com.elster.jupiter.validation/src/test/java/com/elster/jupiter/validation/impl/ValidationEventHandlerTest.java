package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEventHandlerTest {

    private static final Instant date1 = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date2 = ZonedDateTime.of(1983, 5, 31, 15, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date3 = ZonedDateTime.of(1983, 5, 31, 16, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date4 = ZonedDateTime.of(1983, 5, 31, 17, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date5 = ZonedDateTime.of(1983, 5, 31, 18, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant date6 = ZonedDateTime.of(1983, 5, 31, 19, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private ValidationEventHandler handler;

    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private EventType eventType;
    @Mock
    private Channel channel1, channel2, channel3;
    @Mock
    private MeterActivation meterActivation1, meterActivation2;
    @Mock
    private ValidationService validationService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private Clock clock;
    @Mock
    private ReadingType readingType;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(date6);
        handler = new ValidationEventHandler();
        handler.setValidationService(validationService);

        when(eventType.getTopic()).thenReturn("com/elster/jupiter/metering/reading/CREATED");
        Map<Channel, Range<Instant>> map = new HashMap<>();
        map.put(channel1, interval(date1, date2));
        map.put(channel2, interval(date3, date5));
        map.put(channel3, interval(date4, date5));
        when(channel1.getMeterActivation()).thenReturn(meterActivation1);
        when(channel2.getMeterActivation()).thenReturn(meterActivation1);
        when(channel3.getMeterActivation()).thenReturn(meterActivation2);
        when(channel1.getMainReadingType()).thenReturn(readingType);
        when(channel2.getMainReadingType()).thenReturn(readingType);
        when(channel3.getMainReadingType()).thenReturn(readingType);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);

        when(localEvent.getSource()).thenReturn(readingStorer);
        when(localEvent.getType()).thenReturn(eventType);

        when(readingStorer.getScope()).thenReturn(map);
    }

    private Range<Instant> interval(Instant from, Instant to) {
        return Range.openClosed(from, to);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testOnEvent() {
        handler.handle(localEvent);

        verify(validationService).validateForNewData(meterActivation1, interval(date1, date5));
        verify(validationService).validateForNewData(meterActivation2, interval(date4, date5));
    }


}
