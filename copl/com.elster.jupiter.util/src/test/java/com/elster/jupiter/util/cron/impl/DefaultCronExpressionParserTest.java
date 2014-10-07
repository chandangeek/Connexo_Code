package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultCronExpressionParserTest {

    @Test
    public void testTrivial() {
        CronExpression cronExpression = new DefaultCronExpressionParser().parse("* * * * * ? *").orElse(null);
        assertThat(cronExpression.matches(new Date())).isTrue();
    }

    @Test
    public void testIllegalExpression() {
        assertThat(new DefaultCronExpressionParser().parse("* * P * * ? *").isPresent()).isFalse();
    }

}
