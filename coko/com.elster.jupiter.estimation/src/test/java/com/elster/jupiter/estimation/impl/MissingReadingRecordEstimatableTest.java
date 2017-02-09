/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MissingReadingRecordEstimatableTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2011, 9, 18, 14, 39, 22, 116_864, TimeZoneNeutral.getMcMurdo());

    @Test
    public void testTimestamp() {
        MissingReadingRecordEstimatable missingReadingRecordEstimatable = new MissingReadingRecordEstimatable(NOW.toInstant());

        assertThat(missingReadingRecordEstimatable.getTimestamp()).isEqualTo(NOW.toInstant());
    }

    @Test
    public void testSetEstimation() {
        MissingReadingRecordEstimatable missingReadingRecordEstimatable = new MissingReadingRecordEstimatable(NOW.toInstant());

        BigDecimal value = BigDecimal.valueOf(3141592, 6);
        missingReadingRecordEstimatable.setEstimation(value);

        assertThat(missingReadingRecordEstimatable.getEstimation()).isEqualTo(value);
    }


}