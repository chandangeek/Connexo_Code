package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.InvalidCronExpression;

import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCronExpressionParserTest {

    @Test
    public void testTrivial() {
        CronExpression cronExpression = new DefaultCronExpressionParser().parse("* * * * * ? *");
        assertThat(cronExpression.matches(Instant.now())).isTrue();
    }

    @Test(expected = InvalidCronExpression.class)
    public void testIllegalExpression() {
        new DefaultCronExpressionParser().parse("* * P * * ? *");
    }

}
