/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ScheduleExpression {

    Optional<ZonedDateTime> nextOccurrence(ZonedDateTime time);

    String encoded();
}
