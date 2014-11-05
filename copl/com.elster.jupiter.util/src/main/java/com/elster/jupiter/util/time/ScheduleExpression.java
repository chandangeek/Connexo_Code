package com.elster.jupiter.util.time;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ScheduleExpression {

    Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time);

    String encoded();
}
