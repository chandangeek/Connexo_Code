/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.cron.impl;

import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(name = "com.elster.jupiter.cronexpressionparser", service = CronExpressionParser.class)
public class DefaultCronExpressionParser implements CronExpressionParser {

    @Override
    public Optional<CronExpression> parse(String expression) {
        try {
            return Optional.of(new QuartzCronExpressionAdapter(expression));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
