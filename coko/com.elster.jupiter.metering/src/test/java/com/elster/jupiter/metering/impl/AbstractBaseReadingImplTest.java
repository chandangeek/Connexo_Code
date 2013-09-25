package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractBaseReadingImplTest {

    private static final Date DATE = new DateTime(2013, 9, 20, 10, 11, 14).toDate();
    private static final Date RECORD_DATE = new DateTime(2013, 9, 19, 10, 11, 14).toDate();
    private static final BigDecimal VALUE = new BigDecimal("14.15");
    private BaseReadingImpl baseReading;

    @Mock
    private TimeSeriesEntry entry;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType, readingType1, readingType2, unknownReadingType;

    @Before
    public void setUp() {
        when(entry.getTimeStamp()).thenReturn(DATE);
        when(entry.getRecordDateTime()).thenReturn(RECORD_DATE);
        when(entry.getBigDecimal(anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return BigDecimal.valueOf((long)(int) invocationOnMock.getArguments()[0]);
            }
        });
        when(entry.size()).thenReturn(100);
        when(channel.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2, readingType));
        when(entry.getLong(0)).thenReturn(7L);

        baseReading = createInstanceToTest(channel, entry);
    }

    @After
    public void tearDown() {

    }

    abstract BaseReadingImpl createInstanceToTest(Channel channel, TimeSeriesEntry entry);

    @Test
    public void testGetChannel() {
        assertThat(baseReading.getChannel()).isEqualTo(channel);
    }

    @Test
    public void testGetEntry() {
        assertThat(baseReading.getEntry()).isEqualTo(entry);
    }

    @Test
    public void testGetTimeStamp() {
        assertThat(baseReading.getTimeStamp()).isEqualTo(DATE);
    }

    @Test
    public void testGetRecordDate() {
        assertThat(baseReading.getReportedDateTime()).isEqualTo(RECORD_DATE);
    }

    @Test
    public void testGetValue() {
        assertThat(baseReading.getValue()).isEqualTo(BigDecimal.valueOf(baseReading.getReadingTypeOffset()));
    }

    @Test
    public void testGetValues() {
        assertThat(baseReading.getValues()).hasSize(100 - baseReading.getReadingTypeOffset());
    }

    @Test
    public void testGetValueForReadingType() {
        assertThat(baseReading.getValue(readingType)).isEqualTo(BigDecimal.valueOf(baseReading.getReadingTypeOffset() + 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueForUnknownReadingType() {
        baseReading.getValue(unknownReadingType);
    }

    @Test
    public void testGetReadingType() {
        assertThat(baseReading.getReadingType()).isEqualTo(readingType1);
    }

    @Test
    public void testGetReadingTypeAtOffset() {
        assertThat(baseReading.getReadingType(1)).isEqualTo(readingType2);
    }

    @Test
    public void testGetReadingTypes() {
        assertThat(baseReading.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType2, readingType));
    }

    @Test
    public void testGetProcessingFlags() {
        assertThat(baseReading.getProcessingFlags()).isEqualTo(7L);
    }

}
