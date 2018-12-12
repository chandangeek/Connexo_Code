/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.elster.jupiter.util.streams.Functions.asStream;

public class CompositeScheduleExpressionParser implements ScheduleExpressionParser {

    private List<ScheduleExpressionParser> members = new CopyOnWriteArrayList<>();

    @Override
    public Optional<? extends ScheduleExpression> parse(String string) {
        return members.stream()
                .map(m -> m.parse(string))
                .flatMap(asStream())
                .findFirst();
    }

    public boolean add(ScheduleExpressionParser scheduleExpressionParser) {
        return members.add(scheduleExpressionParser);
    }
}
