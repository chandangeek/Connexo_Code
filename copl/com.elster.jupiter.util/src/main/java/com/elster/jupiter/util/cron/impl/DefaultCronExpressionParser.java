package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.InvalidCronExpression;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;

@Component(name = "com.elster.jupiter.cronexpressionparser", service = CronExpressionParser.class)
public class DefaultCronExpressionParser implements CronExpressionParser {

    @Override
    public CronExpression parse(String expression) {
        try {
            return new QuartzCronExpressionAdapter(expression);
        } catch (Exception e) {
            throw new InvalidCronExpression(e).set("expression", expression);
        }
    }
}
