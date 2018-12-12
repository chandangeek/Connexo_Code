/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import org.junit.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCronExpressionParserTest {

    @Test
    public void testTrivial() {
        CronExpression cronExpression = new DefaultCronExpressionParser().parse("* * * * * ? *").orElse(null);
        assertThat(cronExpression.matches(Instant.now())).isTrue();
    }

    @Test
    public void testIllegalExpression() {
        assertThat(new DefaultCronExpressionParser().parse("* * P * * ? *").isPresent()).isFalse();
    }

}
