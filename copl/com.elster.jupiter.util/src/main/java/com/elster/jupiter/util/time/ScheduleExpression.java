package com.elster.jupiter.util.time;

import java.time.ZonedDateTime;

public interface ScheduleExpression {

    ZonedDateTime nextOccurrence(ZonedDateTime time);

    String encoded();
}
