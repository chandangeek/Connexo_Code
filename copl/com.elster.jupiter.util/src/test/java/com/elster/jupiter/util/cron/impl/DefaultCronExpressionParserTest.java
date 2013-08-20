package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.InvalidCronExpression;
import org.junit.Test;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultCronExpressionParserTest {

    @Test
    public void testTrivial() {
        CronExpression cronExpression = new DefaultCronExpressionParser().parse("* * * * * ? *");
        assertThat(cronExpression.matches(new Date())).isTrue();
    }

    @Test(expected = InvalidCronExpression.class)
    public void testIllegalExpression() {
        CronExpression cronExpression = new DefaultCronExpressionParser().parse("* * P * * ? *");
    }

}
