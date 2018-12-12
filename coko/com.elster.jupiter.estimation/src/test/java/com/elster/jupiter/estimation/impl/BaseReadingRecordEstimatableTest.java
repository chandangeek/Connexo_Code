/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.BaseReadingRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaseReadingRecordEstimatableTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2011, 9, 18, 14, 39, 22, 116_864, TimeZoneNeutral.getMcMurdo());

    @Mock
    private BaseReadingRecord baseReadingRecord;

    @Before
    public void setUp() {
        when(baseReadingRecord.getTimeStamp()).thenReturn(NOW.toInstant());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testTimestamp() {
        BaseReadingRecordEstimatable baseReadingRecordEstimatable = new BaseReadingRecordEstimatable(baseReadingRecord);

        assertThat(baseReadingRecordEstimatable.getTimestamp()).isEqualTo(NOW.toInstant());
    }

    @Test
    public void testSetEstimation() {
        BaseReadingRecordEstimatable baseReadingRecordEstimatable = new BaseReadingRecordEstimatable(baseReadingRecord);

        BigDecimal value = BigDecimal.valueOf(3141592, 6);
        baseReadingRecordEstimatable.setEstimation(value);

        assertThat(baseReadingRecordEstimatable.getEstimation()).isEqualTo(value);
    }


}