package com.elster.jupiter.util.time;

import java.util.Optional;

public interface ScheduleExpressionParser {

    Optional<? extends ScheduleExpression> parse(String string);

}
