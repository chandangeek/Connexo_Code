/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Created by bvn on 9/25/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsumptionExportGeneratorTest {

    @Mock
    ScheduledThreadPoolExecutor executor;

    @Test
    public void testStartTimeAndDelay60SecondsFrequency() throws Exception {
        int outputFrequency = 60;
        int timeAcceleration = 60;
        String readingType = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        MinMax minMax = new MinMax(100.0, 200.0);
        UsagePoint usagePoint = new UsagePoint("usp1", "dev1", minMax, Status.connected);
        Configuration configuration = new Configuration(outputFrequency, timeAcceleration, "/tmp", "http://localhost:8085", 8080, Collections.singletonList(usagePoint), readingType);
        ZonedDateTime time = ZonedDateTime.of(2015, 9, 23, 9, 12, 15, 15, ZoneId.systemDefault());
        Clock fixed = Clock.fixed(time.toInstant(), time.getZone());
        ConsumptionExportGenerator generator = new ConsumptionExportGenerator(fixed, executor);
        generator.setConfiguration(configuration);
        generator.start();

        ArgumentCaptor<ConsumptionExportGenerator.ExportGeneratorTask> task = ArgumentCaptor.forClass(ConsumptionExportGenerator.ExportGeneratorTask.class);
        ArgumentCaptor<Long> delay = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> timeUnit = ArgumentCaptor.forClass(TimeUnit.class);
        verify(executor).scheduleAtFixedRate(task.capture(), delay.capture(), period.capture(), timeUnit.capture());
        assertThat(delay.getValue()).isEqualTo(45);
        assertThat(task.getValue().getNow().get(ChronoField.HOUR_OF_DAY)).isEqualTo(9);
        assertThat(task.getValue().getNow().get(ChronoField.MINUTE_OF_HOUR)).isEqualTo(15);
        assertThat(task.getValue().getNow().get(ChronoField.SECOND_OF_MINUTE)).isEqualTo(0);
        assertThat(task.getValue().getReadingsPerTaskRun()).isEqualTo(4);
    }

    @Test
    public void testStartTimeAndDelay15SecondFrequency() throws Exception {
        int outputFrequency = 15;
        int timeAcceleration = 60;
        String readingType = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";

        MinMax minMax = new MinMax(100.0, 200.0);
        UsagePoint usagePoint = new UsagePoint("usp1", "dev1", minMax, Status.connected);
        Configuration configuration = new Configuration(outputFrequency, timeAcceleration, "/tmp", "http://localhost:8085", 8080, Collections.singletonList(usagePoint), readingType);
        ZonedDateTime time = ZonedDateTime.of(2015, 9, 23, 9, 12, 12, 15, ZoneId.systemDefault());
        Clock fixed = Clock.fixed(time.toInstant(), time.getZone());
        ConsumptionExportGenerator generator = new ConsumptionExportGenerator(fixed, executor);
        generator.setConfiguration(configuration);
        generator.start();

        ArgumentCaptor<ConsumptionExportGenerator.ExportGeneratorTask> task = ArgumentCaptor.forClass(ConsumptionExportGenerator.ExportGeneratorTask.class);
        ArgumentCaptor<Long> delay = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> timeUnit = ArgumentCaptor.forClass(TimeUnit.class);
        verify(executor).scheduleAtFixedRate(task.capture(), delay.capture(), period.capture(), timeUnit.capture());
        assertThat(delay.getValue()).isEqualTo(3);
        assertThat(task.getValue().getNow().get(ChronoField.HOUR_OF_DAY)).isEqualTo(9);
        assertThat(task.getValue().getNow().get(ChronoField.MINUTE_OF_HOUR)).isEqualTo(15);
        assertThat(task.getValue().getNow().get(ChronoField.SECOND_OF_MINUTE)).isEqualTo(0);
        assertThat(task.getValue().getReadingsPerTaskRun()).isEqualTo(1);
    }

}
