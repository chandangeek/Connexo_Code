/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TemporalExpressionParser implements ScheduleExpressionParser {

    private static final Pattern PATTERN = Pattern.compile("(\\d+),(\\d+);(?:(\\d+),(\\d+))?");

    @Override
    public Optional<ScheduleExpression> parse(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (matcher.matches()) {
            return Optional.of(parse(matcher));
        }
        return Optional.empty();
    }

    private ScheduleExpression parse(Matcher matcher) {
        TimeDuration every = new TimeDuration(parseGroup(matcher, 1), parseGroup(matcher, 2));
        if (matcher.group(3).isEmpty()) {
            return new TemporalExpression(every);
        }
        TimeDuration offset = new TimeDuration(parseGroup(matcher, 3), parseGroup(matcher, 4));
        return new TemporalExpression(every, offset);
    }

    private int parseGroup(Matcher matcher, int groupNumber) {
        return Integer.parseInt(matcher.group(groupNumber));
    }
}
