package com.elster.jupiter.util.cron.impl;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;

@Component(name = "com.elster.jupiter.cronexpressionparser", service = CronExpressionParser.class)
public class DefaultCronExpressionParser implements CronExpressionParser {

    @Override
    public CronExpression parse(String expression) {
        return new QuartzCronExpressionAdapter(expression);
    }
}
