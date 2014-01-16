package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

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
    private IntervalReadingRecord source;
    @Mock
    private ReadingType readingType1, readingType2, readingType3, readingType4;

    @Before
    public void setUp() {
        filteredReading = new FilteredIntervalReadingRecord(source, 1, 3, 0);

        when(source.getReadingType(1)).thenReturn(readingType1);
        when(source.getReadingType(2)).thenReturn(readingType2);
        when(source.getReadingType(3)).thenReturn(readingType3);
        when(source.getReadingType(4)).thenReturn(readingType4);

        when(source.getQuantity(1)).thenReturn(VALUE1);
        when(source.getQuantity(2)).thenReturn(VALUE2);
        when(source.getQuantity(3)).thenReturn(VALUE3);
        when(source.getQuantity(4)).thenReturn(VALUE4);

        when(source.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2, readingType3, readingType4));
        when(source.getQuantities()).thenReturn(Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4));
        when(source.getReadingType()).thenReturn(readingType1);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSimpleDelegationForProcessingFlags() {
        ProcesStatus processingFlags = ProcesStatus.of(ProcesStatus.Flag.ESTIMATED,ProcesStatus.Flag.WARNING);
        when(source.getProcesStatus()).thenReturn(processingFlags);

        assertThat(filteredReading.getProcesStatus()).isEqualTo(processingFlags);
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
        assertThat(filteredReading.getReadingType()).isEqualTo(readingType1);
    }

    @Test
    public void testGetReadingTypes() {
        assertThat(filteredReading.getReadingTypes()).isEqualTo(Arrays.asList(readingType2, readingType4, readingType1));
    }

    @Test
    public void testGetReportedDateTime() {
        Date date = new Date(5416541641L);
        when(source.getReportedDateTime()).thenReturn(date);

        assertThat(filteredReading.getReportedDateTime()).isEqualTo(date);
    }

    @Test
    public void testGetTimeStamp() {
        Date date = new Date(5416541641L);
        when(source.getTimeStamp()).thenReturn(date);

        assertThat(filteredReading.getTimeStamp()).isEqualTo(date);
    }

    @Test
    public void testGetValue() {
        when(source.getValue()).thenReturn(VALUE1.getValue());

        assertThat(filteredReading.getValue()).isEqualTo(VALUE1.getValue());
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

    @Test
    public void testGetProfileStatus() {
        ProfileStatus profileStatus = ProfileStatus.of(ProfileStatus.Flag.BADTIME);
        when(source.getProfileStatus()).thenReturn(ProfileStatus.of(ProfileStatus.Flag.BADTIME));

        assertThat(filteredReading.getProfileStatus()).isEqualTo(profileStatus);
    }

}
