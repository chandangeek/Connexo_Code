/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomMeterReadingItemDataSelectorTest {

    private CustomMeterReadingItemDataSelector customMeterReadingItemDataSelector;
    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private Clock clock = Clock.system(ZoneId.systemDefault());
    public static final ZonedDateTime FIRST = ZonedDateTime.of(2020, 2, 3, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final ZonedDateTime LAST = ZonedDateTime.of(2020, 2, 3, 6, 0, 0, 0, ZoneId.systemDefault());

    @Mock(extraInterfaces = DefaultSelectorOccurrence.class)
    private DataExportOccurrence occurrence;
    @Mock
    private ReadingTypeDataExportItem item;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingType readingType;
    @Mock
    private ReadingContainer readingContainer;
    @Mock(extraInterfaces = {IntervalReadingRecord.class})
    private ReadingRecord reading1, reading2, reading3, reading4, reading5, reading6, reading7;
    @Mock
    private Logger logger;

    @Before
    public void setup() {
        customMeterReadingItemDataSelector = new CustomMeterReadingItemDataSelector(clock, thesaurus, transactionService);
        customMeterReadingItemDataSelector.init(sapCustomPropertySets, logger);
        when(((DefaultSelectorOccurrence)occurrence).getExportedDataInterval()).thenReturn(Range.openClosed(Instant.from(FIRST), Instant.from(LAST)));
        when(item.getLastExportedNewDate()).thenReturn(Optional.of(Instant.from(LAST)));
        when(item.getReadingType()).thenReturn(readingType);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMRID()).thenReturn("1234567890");
        RangeSet<Instant> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.openClosed(Instant.from(FIRST), Instant.from(LAST)));
        Map<String, RangeSet<Instant>> profileIds = new HashMap<>();
        profileIds.put("1001", rangeSet);
        when(sapCustomPropertySets.getProfileId(any(), any(), any())).thenReturn(profileIds);
        when(sapCustomPropertySets.isRegistered(any(Device.class))).thenReturn(true);
        when(sapCustomPropertySets.isRegistered(any(EndDevice.class))).thenReturn(true);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Test
    public void testSelectEmptyData() {
        when(item.getReadingContainer()).thenReturn(readingContainer);
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(6);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isExactlyInstanceOf(ZeroIntervalReadingImpl.class);
    }

    @Test
    public void testSelectFullData() {
        when(item.getReadingContainer()).thenReturn(readingContainer);
        doReturn(Arrays.asList(reading1, reading2, reading3, reading4, reading5, reading6, reading7)).when(readingContainer).getReadings(any(), any());
        when(reading1.getValue()).thenReturn(BigDecimal.ONE);
        when(reading1.getTimeStamp()).thenReturn(Instant.from(FIRST));
        when(reading2.getValue()).thenReturn(BigDecimal.valueOf(2));
        when(reading2.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(1, ChronoUnit.HOURS));
        when(reading3.getValue()).thenReturn(BigDecimal.valueOf(3));
        when(reading3.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(2, ChronoUnit.HOURS));
        when(reading4.getValue()).thenReturn(BigDecimal.valueOf(4));
        when(reading4.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(3, ChronoUnit.HOURS));
        when(reading5.getValue()).thenReturn(BigDecimal.valueOf(5));
        when(reading5.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(4, ChronoUnit.HOURS));
        when(reading6.getValue()).thenReturn(BigDecimal.valueOf(6));
        when(reading6.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(5, ChronoUnit.HOURS));
        when(reading7.getValue()).thenReturn(BigDecimal.valueOf(7));
        when(reading7.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(6, ChronoUnit.HOURS));
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(7);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isSameAs(reading1);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading2);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isSameAs(reading6);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord()).isSameAs(reading7);
    }

    @Test
    public void testSelectGaps() {
        when(item.getReadingContainer()).thenReturn(readingContainer);
        doReturn(Arrays.asList(reading3, reading4, reading5)).when(readingContainer).getReadings(any(), any());
        when(reading3.getValue()).thenReturn(BigDecimal.valueOf(3));
        when(reading3.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(2, ChronoUnit.HOURS));
        when(reading4.getValue()).thenReturn(BigDecimal.valueOf(4));
        when(reading4.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(3, ChronoUnit.HOURS));
        when(reading5.getValue()).thenReturn(BigDecimal.valueOf(5));
        when(reading5.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(4, ChronoUnit.HOURS));
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(6);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    public void testSelectTwoProfiles() {
        RangeSet<Instant> rangeSet1 = TreeRangeSet.create();
        rangeSet1.add(Range.openClosed(Instant.from(FIRST), Instant.from(FIRST).plus(2, ChronoUnit.HOURS)));
        RangeSet<Instant> rangeSet2 = TreeRangeSet.create();
        rangeSet2.add(Range.openClosed(Instant.from(FIRST).plus(2, ChronoUnit.HOURS), Instant.from(LAST)));
        Map<String, RangeSet<Instant>> profileIds = new HashMap<>();
        profileIds.put("1001", rangeSet1);
        profileIds.put("2002", rangeSet2);
        when(sapCustomPropertySets.getProfileId(any(), any(), any())).thenReturn(profileIds);
        when(item.getReadingContainer()).thenReturn(readingContainer);
        doReturn(Arrays.asList(reading3, reading4, reading5)).when(readingContainer).getReadings(any(), any());
        when(reading3.getValue()).thenReturn(BigDecimal.valueOf(3));
        when(reading3.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(2, ChronoUnit.HOURS));
        when(reading4.getValue()).thenReturn(BigDecimal.valueOf(4));
        when(reading4.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(3, ChronoUnit.HOURS));
        when(reading5.getValue()).thenReturn(BigDecimal.valueOf(5));
        when(reading5.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(4, ChronoUnit.HOURS));
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(6);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    public void testSelectTwoProfilesWithGapsBetween() {
        RangeSet<Instant> rangeSet1 = TreeRangeSet.create();
        rangeSet1.add(Range.openClosed(Instant.from(FIRST), Instant.from(FIRST).plus(2, ChronoUnit.HOURS)));
        RangeSet<Instant> rangeSet2 = TreeRangeSet.create();
        rangeSet2.add(Range.openClosed(Instant.from(FIRST).plus(2, ChronoUnit.HOURS), Instant.from(LAST)));
        Map<String, RangeSet<Instant>> profileIds = new HashMap<>();
        profileIds.put("1001", rangeSet1);
        profileIds.put("2002", rangeSet2);
        when(sapCustomPropertySets.getProfileId(any(), any(), any())).thenReturn(profileIds);
        when(item.getReadingContainer()).thenReturn(readingContainer);
        doReturn(Arrays.asList(reading1, reading2, reading5)).when(readingContainer).getReadings(any(), any());
        when(reading1.getValue()).thenReturn(BigDecimal.ONE);
        when(reading1.getTimeStamp()).thenReturn(Instant.from(FIRST));
        when(reading2.getValue()).thenReturn(BigDecimal.valueOf(2));
        when(reading2.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(1, ChronoUnit.HOURS));
        when(reading5.getValue()).thenReturn(BigDecimal.valueOf(5));
        when(reading5.getTimeStamp()).thenReturn(Instant.from(FIRST).plus(4, ChronoUnit.HOURS));
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(7);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isSameAs(reading1);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading2);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord()).isExactlyInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl)result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord().getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }
}
