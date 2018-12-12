/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilteredReadingTest {

    private static final Quantity VALUE1 = Unit.WATT_HOUR.amount(BigDecimal.valueOf(1, 0), 3);
    private static final Quantity VALUE2 = Unit.WATT_HOUR.amount(BigDecimal.valueOf(2, 0), 3);
    private static final Quantity VALUE3 = Unit.WATT_HOUR.amount(BigDecimal.valueOf(3, 0), 3);
    private static final Quantity VALUE4 = Unit.WATT_HOUR.amount(BigDecimal.valueOf(4, 0), 3);
    private FilteredIntervalReadingRecord filteredReading;

    @Mock
    private IntervalReadingRecordImpl source;
    @Mock
    private DataModel dataModel;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    
    private ReadingTypeImpl readingType1, readingType2, readingType3, readingType4;

    @Before
    public void setUp() {

        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .in(MetricMultiplier.ZERO, ReadingTypeUnit.WATTHOUR);

        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.code(), "");
    	readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.in(MetricMultiplier.KILO).code(), "");
    	readingType3 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.in(MetricMultiplier.MEGA).code(), "");
    	readingType4 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.in(MetricMultiplier.GIGA).code(), "");

        filteredReading = new FilteredIntervalReadingRecord(source, 1, 3, 0);

        when(source.getReadingType(1)).thenReturn(readingType1);
        when(source.getReadingType(2)).thenReturn(readingType2);
        when(source.getReadingType(3)).thenReturn(readingType3);
        when(source.getReadingType(4)).thenReturn(readingType4);

        when(source.getQuantity(1)).thenReturn(VALUE1);
        when(source.getQuantity(2)).thenReturn(VALUE2);
        when(source.getQuantity(3)).thenReturn(VALUE3);
        when(source.getQuantity(4)).thenReturn(VALUE4);

        when(source.getQuantity(readingType1)).thenReturn(VALUE1);
        when(source.getQuantity(readingType2)).thenReturn(VALUE2);
        when(source.getQuantity(readingType3)).thenReturn(VALUE3);
        when(source.getQuantity(readingType4)).thenReturn(VALUE4);

        when(source.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2, readingType3, readingType4));
        when(source.getQuantities()).thenReturn(Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4));
        when(source.getReadingType()).thenReturn(readingType1);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSimpleDelegationForProcessingFlags() {
        ProcessStatus processingFlags = ProcessStatus.of(ProcessStatus.Flag.ESTIMATED, ProcessStatus.Flag.WARNING);
        when(source.getProcessStatus()).thenReturn(processingFlags);

        assertThat(filteredReading.getProcessStatus()).isEqualTo(processingFlags);
    }

    @Test
    public void testGetReadingTypeIsMappedProperly() {
        assertThat(filteredReading.getReadingType(0)).isEqualTo(readingType2);
        assertThat(filteredReading.getReadingType(1)).isEqualTo(readingType4);
        assertThat(filteredReading.getReadingType(2)).isEqualTo(readingType1);
    }

    @Test
    public void testGetValueIsMappedProperly() {
        assertThat(filteredReading.getQuantity(0)).isEqualTo(VALUE2);
        assertThat(filteredReading.getQuantity(1)).isEqualTo(VALUE4);
        assertThat(filteredReading.getQuantity(2)).isEqualTo(VALUE1);
    }

    @Test
    public void testGetReadingTypeSimplyDelegates() {
        assertThat(filteredReading.getReadingType()).isEqualTo(readingType2);
    }

    @Test
    public void testGetReadingTypes() {
        assertThat(filteredReading.getReadingTypes()).isEqualTo(Arrays.asList(readingType2, readingType4, readingType1));
    }

    @Test
    public void testGetReportedDateTime() {
        Instant instant = Instant.ofEpochMilli(5416541641L);
        when(source.getReportedDateTime()).thenReturn(instant);

        assertThat(filteredReading.getReportedDateTime()).isEqualTo(instant);
    }

    @Test
    public void testGetTimeStamp() {
        Instant instant = Instant.ofEpochMilli(5416541641L);
        when(source.getTimeStamp()).thenReturn(instant);

        assertThat(filteredReading.getTimeStamp()).isEqualTo(instant);
    }

    @Test
    public void testGetValue() {
        when(source.getValue()).thenReturn(VALUE1.getValue());

        assertThat(filteredReading.getValue()).isEqualTo(VALUE2.getValue());
    }

    @Test
    public void testGetValueForReadingType() {
        when(source.getQuantity(readingType2)).thenReturn(VALUE2);

        assertThat(filteredReading.getQuantity(readingType2)).isEqualTo(VALUE2);
    }

    @Test
    public void testGetValues() {
        assertThat(filteredReading.getQuantities()).isEqualTo(Arrays.asList(VALUE2, VALUE4, VALUE1));
    }

}
