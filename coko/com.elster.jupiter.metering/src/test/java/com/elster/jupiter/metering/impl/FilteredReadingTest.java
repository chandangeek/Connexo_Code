package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.ReadingType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilteredReadingTest {

    private static final BigDecimal VALUE1 = BigDecimal.valueOf(1, 0);
    private static final BigDecimal VALUE2 = BigDecimal.valueOf(2, 0);
    private static final BigDecimal VALUE3 = BigDecimal.valueOf(3, 0);
    private static final BigDecimal VALUE4 = BigDecimal.valueOf(4, 0);
    private FilteredIntervalReading filteredReading;

    @Mock
    private IntervalReading source;
    @Mock
    private ReadingType readingType1, readingType2, readingType3, readingType4;

    @Before
    public void setUp() {
        filteredReading = new FilteredIntervalReading(source, 1, 3, 0);

        when(source.getReadingType(1)).thenReturn(readingType1);
        when(source.getReadingType(2)).thenReturn(readingType2);
        when(source.getReadingType(3)).thenReturn(readingType3);
        when(source.getReadingType(4)).thenReturn(readingType4);

        when(source.getValue(1)).thenReturn(VALUE1);
        when(source.getValue(2)).thenReturn(VALUE2);
        when(source.getValue(3)).thenReturn(VALUE3);
        when(source.getValue(4)).thenReturn(VALUE4);

        when(source.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2, readingType3, readingType4));
        when(source.getValues()).thenReturn(Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4));
        when(source.getReadingType()).thenReturn(readingType1);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testSimpleDelegationForProcessingFlags() {
        long processingFlags = 15L;
        when(source.getProcessingFlags()).thenReturn(processingFlags);

        assertThat(filteredReading.getProcessingFlags()).isEqualTo(processingFlags);
    }

    @Test
    public void testGetReadingTypeIsMappedProperly() {
        assertThat(filteredReading.getReadingType(0)).isEqualTo(readingType2);
        assertThat(filteredReading.getReadingType(1)).isEqualTo(readingType4);
        assertThat(filteredReading.getReadingType(2)).isEqualTo(readingType1);
    }

    @Test
    public void testGetValueIsMappedProperly() {
        assertThat(filteredReading.getValue(0)).isEqualTo(VALUE2);
        assertThat(filteredReading.getValue(1)).isEqualTo(VALUE4);
        assertThat(filteredReading.getValue(2)).isEqualTo(VALUE1);
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
        when(source.getValue()).thenReturn(VALUE1);

        assertThat(filteredReading.getValue()).isEqualTo(VALUE1);
    }

    @Test
    public void testGetValueForReadingType() {
        when(source.getValue(readingType2)).thenReturn(VALUE2);

        assertThat(filteredReading.getValue(readingType2)).isEqualTo(VALUE2);
    }

    @Test
    public void testGetValues() {
        assertThat(filteredReading.getValues()).isEqualTo(Arrays.asList(VALUE2, VALUE4, VALUE1));
    }

    @Test
    public void testGetProfileStatus() {
        long profileStatus = 564L;
        when(source.getProfileStatus()).thenReturn(profileStatus);

        assertThat(filteredReading.getProfileStatus()).isEqualTo(profileStatus);
    }

}
