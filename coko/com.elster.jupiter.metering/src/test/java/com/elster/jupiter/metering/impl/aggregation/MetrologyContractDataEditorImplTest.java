/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.IncompatibleTimeOfUseException;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link MetrologyContractDataEditorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-28 (10:50)
 */
@RunWith(MockitoJUnitRunner.class)
public class MetrologyContractDataEditorImplTest {

    private static final long OFF_PEAK_CODE = 10L;
    private static final long PEAK_CODE = 11L;

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Channel channel;
    @Mock
    private UsagePoint.UsedCalendars usedCalendars;
    @Mock
    private Calendar calendar;
    @Mock
    private MetrologyContract contract;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private ServerDataAggregationService dataAggregationService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void initializeMocks() {
        when(this.usagePoint.getUsedCalendars()).thenReturn(this.usedCalendars);
        ZoneId zoneId = TimeZoneNeutral.getMcMurdo();
        when(this.usagePoint.getMRID()).thenReturn(MetrologyContractDataEditorImplTest.class.getSimpleName());
        when(this.usagePoint.getZoneId()).thenReturn(zoneId);
        when(this.usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenReturn(Collections.singletonList(this.effectiveMetrologyConfigurationOnUsagePoint));
        when(this.effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(this.contract)).thenReturn(Optional.of(this.channelsContainer));
        when(this.effectiveMetrologyConfigurationOnUsagePoint.getRange()).thenReturn(Range.all());
        when(this.dataAggregationService.hasContract(this.effectiveMetrologyConfigurationOnUsagePoint, this.contract)).thenReturn(true);

        when(this.dataAggregationService.getThesaurus()).thenReturn(this.thesaurus);
        when(this.dataAggregationService.getClock()).thenReturn(Clock.system(zoneId));
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not relevant in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    /**
     * Initializes the mocks in such a way that the specified <code>expectedTimeOfUse</code>
     * bucket is used on the deliverable and the specified <code>actualTimeOfUse</code>
     * is produced by the Calendar.
     *
     * @param expectedTimeOfUse The time of use code that will be mocked on the deliverable's reading type
     * @param actualTimeOfUse The time of use code that will be produced by the calendar
     */
    private void setupMocksForTimeOfUse(long expectedTimeOfUse, long actualTimeOfUse) {
        ReadingType readingType = this.mockedReadingType(expectedTimeOfUse);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        when(this.channelsContainer.getChannel(readingType)).thenReturn(Optional.of(this.channel));

        when(this.usedCalendars.getCalendar(any(Instant.class), any(Category.class))).thenReturn(Optional.of(this.calendar));
        Event actualEvent = mock(Event.class);
        when(actualEvent.getCode()).thenReturn(actualTimeOfUse);
        when(this.calendar.getEvents()).thenReturn(Collections.singletonList(actualEvent));
        Calendar.ZonedView zonedView = mock(Calendar.ZonedView.class);
        when(zonedView.eventFor(any(Instant.class))).thenReturn(actualEvent);
        when(this.calendar.forZone(any(ZoneId.class), any(Year.class), any(Year.class))).thenReturn(zonedView);
    }

    /**
     * Initializes the mocks in such a way that the time of use is not active.
     */
    private void setupMocksWithoutTimeOfUse() {
        ReadingType readingType = this.mockedReadingType(0);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        when(this.channelsContainer.getChannel(readingType)).thenReturn(Optional.of(this.channel));

        when(this.usedCalendars.getCalendar(any(Instant.class), any(Category.class))).thenReturn(Optional.empty());
    }

    @Test(expected = IncompatibleTimeOfUseException.class)
    public void removeTimestampOutSideTimeOfUseBucket() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();

        // Business method
        editor.remove(Instant.now());

        // Asserts: see expected exception rule
    }

    @Test(expected = IncompatibleTimeOfUseException.class)
    public void removeReadingOutSideTimeOfUseBucket() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();

        // Business method
        editor.remove(mockedReadingRecord());

        // Asserts: see expected exception rule
    }

    @Test(expected = IncompatibleTimeOfUseException.class)
    public void estimateOutSideTimeOfUseBucket() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();

        // Business method
        editor.estimate(mockedReading());

        // Asserts: see expected exception rule
    }

    @Test(expected = IncompatibleTimeOfUseException.class)
    public void confirmOutSideTimeOfUseBucket() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();

        // Business method
        editor.confirm(mockedReading());

        // Asserts: see expected exception rule
    }

    @Test(expected = IncompatibleTimeOfUseException.class)
    public void updateOutSideTimeOfUseBucket() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();

        // Business method
        editor.update(mockedReading());

        // Asserts: see expected exception rule
    }

    @Test
    public void removeTimestampWithoutTimeOfUse() {
        this.setupMocksWithoutTimeOfUse();
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        BaseReadingRecord reading = this.mockedReadingRecord();
        when(this.channel.getReading(now)).thenReturn(Optional.of(reading));
        editor.remove(now);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).getReading(now);
        verify(this.channel).removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void removeTimestampWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        BaseReadingRecord reading = this.mockedReadingRecord();
        when(this.channel.getReading(now)).thenReturn(Optional.of(reading));
        editor.remove(now);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).getReading(now);
        verify(this.channel).removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void removeTimestampsWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        Instant fifteenMinutesAgo = now.minusSeconds(900);
        BaseReadingRecord readingNow = this.mockedReadingRecord(now);
        BaseReadingRecord readingFifteenMinutesAgo = this.mockedReadingRecord(fifteenMinutesAgo);
        when(this.channel.getReading(now)).thenReturn(Optional.of(readingNow));
        when(this.channel.getReading(fifteenMinutesAgo)).thenReturn(Optional.of(readingFifteenMinutesAgo));
        editor.removeTimestamps(new HashSet<>(Arrays.asList(now, fifteenMinutesAgo)));

        // Business method
        editor.save();

        // Asserts
        ArgumentCaptor<List> readingTimestampsCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.channel).removeReadings(eq(QualityCodeSystem.MDM), readingTimestampsCaptor.capture());
        assertThat(readingTimestampsCaptor.getValue()).contains(readingNow, readingFifteenMinutesAgo);
    }

    @Test
    public void removeReadingWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.remove(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void removeReadingWithoutTimeOfUse() {
        this.setupMocksWithoutTimeOfUse();
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.remove(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).removeReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void removeReadingsWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        Instant fifteenMinutesAgo = now.minusSeconds(900);
        BaseReadingRecord readingNow = this.mockedReadingRecord(now);
        BaseReadingRecord readingFifteenMinutesAgo = this.mockedReadingRecord(fifteenMinutesAgo);
        editor.removeReadings(new HashSet<>(Arrays.asList(readingNow, readingFifteenMinutesAgo)));

        // Business method
        editor.save();

        // Asserts
        ArgumentCaptor<List> readingCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.channel).removeReadings(eq(QualityCodeSystem.MDM), readingCaptor.capture());
        assertThat(readingCaptor.getValue()).contains(readingNow, readingFifteenMinutesAgo);
    }

    @Test
    public void estimateReadingWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.estimate(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).estimateReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void estimateReadingWithoutTimeOfUse() {
        this.setupMocksWithoutTimeOfUse();
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.estimate(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).estimateReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void estimateReadingsWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        Instant fifteenMinutesAgo = now.minusSeconds(900);
        BaseReadingRecord readingNow = this.mockedReadingRecord(now);
        BaseReadingRecord readingFifteenMinutesAgo = this.mockedReadingRecord(fifteenMinutesAgo);
        editor.estimateAll(Arrays.asList(readingNow, readingFifteenMinutesAgo));

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).estimateReadings(QualityCodeSystem.MDM, Arrays.asList(readingNow, readingFifteenMinutesAgo));
    }

    @Test
    public void confirmReadingWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.confirm(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).confirmReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void confirmReadingWithoutTimeOfUse() {
        this.setupMocksWithoutTimeOfUse();
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.confirm(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).confirmReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void confirmReadingsWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        Instant fifteenMinutesAgo = now.minusSeconds(900);
        BaseReadingRecord readingNow = this.mockedReadingRecord(now);
        BaseReadingRecord readingFifteenMinutesAgo = this.mockedReadingRecord(fifteenMinutesAgo);
        editor.confirmAll(Arrays.asList(readingNow, readingFifteenMinutesAgo));

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).confirmReadings(QualityCodeSystem.MDM, Arrays.asList(readingNow, readingFifteenMinutesAgo));
    }

    @Test
    public void updateReadingWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.update(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).editReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void updateReadingWithoutTimeOfUse() {
        this.setupMocksWithoutTimeOfUse();
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord reading = this.mockedReadingRecord();
        editor.update(reading);

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).editReadings(QualityCodeSystem.MDM, Collections.singletonList(reading));
    }

    @Test
    public void updateReadingsWithCorrectTimeOfUse() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        Instant now = Instant.now();
        Instant fifteenMinutesAgo = now.minusSeconds(900);
        BaseReadingRecord readingNow = this.mockedReadingRecord(now);
        BaseReadingRecord readingFifteenMinutesAgo = this.mockedReadingRecord(fifteenMinutesAgo);
        editor.updateAll(Arrays.asList(readingNow, readingFifteenMinutesAgo));

        // Business method
        editor.save();

        // Asserts
        verify(this.channel).editReadings(QualityCodeSystem.MDM, Arrays.asList(readingNow, readingFifteenMinutesAgo));
    }

    @Test(expected = IllegalStateException.class)
    public void cannotRemoveTimestampAfterSave() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord readingNow = this.mockedReadingRecord();
        editor.remove(readingNow);
        editor.save();

        // Business method
        editor.remove(readingNow);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void cannotRemoveReadingAfterSave() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord readingNow = this.mockedReadingRecord();
        editor.remove(readingNow);
        editor.save();

        // Business method
        editor.remove(readingNow);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void cannotEstimateReadingAfterSave() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord readingNow = this.mockedReadingRecord();
        editor.estimate(readingNow);
        editor.save();

        // Business method
        editor.estimate(readingNow);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void cannotConfirmReadingAfterSave() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord readingNow = this.mockedReadingRecord();
        editor.confirm(readingNow);
        editor.save();

        // Business method
        editor.confirm(readingNow);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void cannotUpdateReadingAfterSave() {
        this.setupMocksForTimeOfUse(OFF_PEAK_CODE, OFF_PEAK_CODE);
        MetrologyContractDataEditorImpl editor = this.getInstance();
        BaseReadingRecord readingNow = this.mockedReadingRecord();
        editor.update(readingNow);
        editor.save();

        // Business method
        editor.update(readingNow);

        // Asserts: see expected exception rule
    }

    private MetrologyContractDataEditorImpl getInstance() {
        return new MetrologyContractDataEditorImpl(this.usagePoint, this.contract, this.deliverable, QualityCodeSystem.MDM, this.dataAggregationService);
    }

    private BaseReading mockedReading() {
        BaseReading reading = mock(BaseReading.class);
        when(reading.getTimeStamp()).thenReturn(Instant.now());
        return reading;
    }

    private BaseReadingRecord mockedReadingRecord() {
        return this.mockedReadingRecord(Instant.now());
    }

    private BaseReadingRecord mockedReadingRecord(Instant timestamp) {
        BaseReadingRecord reading = mock(BaseReadingRecord.class);
        when(reading.getTimeStamp()).thenReturn(timestamp);
        return reading;
    }

    private ReadingType mockedReadingType(long tou) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getTou()).thenReturn((int) tou);
        when(readingType.getIntervalLength()).thenReturn(Optional.empty());
        return readingType;
    }
}