/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.LocalEventImpl;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PublisherImpl;

import org.osgi.service.event.Event;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityImplIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Mock
    private Subscriber topicHandler;

    @Test
    @Transactional
    public void test() throws SQLException {
        when(topicHandler.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
        ((PublisherImpl) inMemoryBootstrapModule.getPublisher()).addHandler(topicHandler);

        Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        doTest(date);

        ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
        verify(topicHandler, times(5)).handle(localEventCapture.capture());

        LocalEvent localEvent = localEventCapture.getAllValues().get(4);
        assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.READING_QUALITY_CREATED.topic());
        Event event = localEvent.toOsgiEvent();
        assertThat(event.containsProperty("readingTimestamp")).isTrue();
        assertThat(event.containsProperty("channelId")).isTrue();
        assertThat(event.containsProperty("readingQualityTypeCode")).isTrue();
    }

    @Test
    @Transactional
    public void testDelete() {
        Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ReadingQualityRecord readingQuality = doTest(date);

        when(topicHandler.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
        ((PublisherImpl) inMemoryBootstrapModule.getPublisher()).addHandler(topicHandler);


        readingQuality.delete();
        ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
        verify(topicHandler).handle(localEventCapture.capture());

        LocalEvent localEvent = localEventCapture.getValue();
        assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.READING_QUALITY_DELETED.topic());
        Event event = localEvent.toOsgiEvent();
        assertThat(event.containsProperty("readingTimestamp")).isTrue();
        assertThat(event.containsProperty("channelId")).isTrue();
        assertThat(event.containsProperty("readingQualityTypeCode")).isTrue();
    }

    private ReadingQualityRecord doTest(Instant date) {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("name", Instant.EPOCH).create();
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("meter" + date.toEpochMilli(), "myName" + date.toEpochMilli()).create();
        MeterActivation meterActivation = usagePoint.activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT), date);
        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
        ReadingStorer regularStorer = meteringService.createNonOverrulingStorer();
        regularStorer.addReading(channel.getCimChannel(readingType).get(), IntervalReadingImpl.of(date, BigDecimal.valueOf(561561, 2)));
        regularStorer.execute(QualityCodeSystem.MDC);
        BaseReadingRecord reading = channel.getReading(date).get();
        return channel.createReadingQuality(new ReadingQualityType("6.1"), readingType, reading);
    }

}
