/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
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

import com.google.common.collect.ImmutableMap;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomMeterReadingItemDataSelectorTest {
    private static final ZonedDateTime PREVIOUS = ZonedDateTime.of(2020, 2, 2, 6, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime FIRST = ZonedDateTime.of(2020, 2, 3, 0, 0, 0, 0, ZoneId.systemDefault());
    private static final ZonedDateTime LAST = ZonedDateTime.of(2020, 2, 3, 6, 0, 0, 0, ZoneId.systemDefault());
    private static final Range<Instant> FIRST_TO_LAST = Range.openClosed(Instant.from(FIRST), Instant.from(LAST));
    private static final Range<Instant> PREVIOUS_TO_LAST = Range.openClosed(Instant.from(PREVIOUS), Instant.from(LAST));

    private CustomMeterReadingItemDataSelector customMeterReadingItemDataSelector;
    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private Clock clock = Clock.system(ZoneId.systemDefault());

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
    @Mock
    private ReadingDataSelectorConfig selectorConfig;

    @Before
    public void setup() {
        customMeterReadingItemDataSelector = new CustomMeterReadingItemDataSelector(clock, thesaurus, transactionService);
        customMeterReadingItemDataSelector.init(sapCustomPropertySets, logger);
        when(((DefaultSelectorOccurrence) occurrence).getExportedDataInterval()).thenReturn(FIRST_TO_LAST);
        when(item.getLastExportedNewData()).thenReturn(Optional.of(Instant.from(PREVIOUS)));
        when(item.getReadingType()).thenReturn(readingType);
        when(readingType.isRegular()).thenReturn(true);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMRID()).thenReturn("1234567890");
        when(sapCustomPropertySets.getProfileId(eq(readingContainer), eq(readingType), any()))
                .thenReturn(ImmutableMap.of("1001", TreeRangeSet.create(Collections.singletonList(PREVIOUS_TO_LAST))));
        when(sapCustomPropertySets.getProfileId(readingContainer, readingType, FIRST_TO_LAST))
                .thenReturn(ImmutableMap.of("1001", TreeRangeSet.create(Collections.singletonList(FIRST_TO_LAST))));
        when(sapCustomPropertySets.isRegistered(any(Device.class))).thenReturn(true);
        when(sapCustomPropertySets.isRegistered(any(EndDevice.class))).thenReturn(true);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(item.getSelector()).thenReturn(selectorConfig);
        when(selectorConfig.isExportContinuousData()).thenReturn(false);
        when(item.getReadingContainer()).thenReturn(readingContainer);
    }

    @Test
    public void testSelectEmptyData() {
        Optional<MeterReadingData> result = customMeterReadingItemDataSelector.selectData(occurrence, item);
        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(6);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord())
                .isInstanceOf(ZeroIntervalReadingImpl.class);
    }

    @Test
    public void testSelectFullData() {
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

        verify(readingContainer).getReadings(FIRST_TO_LAST, readingType);

        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(7);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isSameAs(reading1);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading2);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isSameAs(reading6);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord()).isSameAs(reading7);
    }

    @Test
    public void testSelectContinuousData() {
        when(selectorConfig.isExportContinuousData()).thenReturn(true);
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

        verify(readingContainer).getReadings(PREVIOUS_TO_LAST, readingType);

        assertTrue(result.isPresent());
        assertThat(result.get().getMeterReading().getIntervalBlocks().size()).isEqualTo(1);
        assertThat(result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().size()).isEqualTo(24);
        IntStream.range(0, 17).forEach(index -> {
            assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(index)).getIntervalReadingRecord())
                    .isInstanceOf(GapsIntervalReadingImpl.class);
            assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(index)).getIntervalReadingRecord().getTimeStamp())
                    .isEqualTo(PREVIOUS.plus(index + 1, ChronoUnit.HOURS).toInstant());
            assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(index)).getIntervalReadingRecord().getValue())
                    .isEqualTo(BigDecimal.ONE);
        });
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(17)).getIntervalReadingRecord()).isSameAs(reading1);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(18)).getIntervalReadingRecord()).isSameAs(reading2);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(19)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(20)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(21)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(22)).getIntervalReadingRecord()).isSameAs(reading6);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(23)).getIntervalReadingRecord()).isSameAs(reading7);
    }

    @Test
    public void testSelectGaps() {
        when(sapCustomPropertySets.getProfileId(eq(readingContainer), eq(readingType), any()))
                .thenReturn(ImmutableMap.of("1001", TreeRangeSet.create(Collections.singletonList(FIRST_TO_LAST))));
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
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
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
        when(sapCustomPropertySets.getProfileId(eq(readingContainer), eq(readingType), any())).thenReturn(profileIds);
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
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(3));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading3);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isSameAs(reading4);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
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
        when(sapCustomPropertySets.getProfileId(eq(readingContainer), eq(readingType), any())).thenReturn(profileIds);
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
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(0)).getIntervalReadingRecord()).isSameAs(reading1);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(1)).getIntervalReadingRecord()).isSameAs(reading2);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(2)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(2));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(3)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(4)).getIntervalReadingRecord()).isSameAs(reading5);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(5)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord()).isInstanceOf(GapsIntervalReadingImpl.class);
        assertThat(((IntervalReadingImpl) result.get().getMeterReading().getIntervalBlocks().get(0).getIntervals().get(6)).getIntervalReadingRecord().getValue()).isEqualTo(BigDecimal.valueOf(5));
    }
}
