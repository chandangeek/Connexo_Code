package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

class CompositeScheduleExpressionParser implements ScheduleExpressionParser {

    private List<ScheduleExpressionParser> members = new CopyOnWriteArrayList<>();

    @Override
    public Optional<? extends ScheduleExpression> parse(String string) {
        return members.stream()
                .map(m -> m.parse(string))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public boolean add(ScheduleExpressionParser scheduleExpressionParser) {
        return members.add(scheduleExpressionParser);
    }
}
