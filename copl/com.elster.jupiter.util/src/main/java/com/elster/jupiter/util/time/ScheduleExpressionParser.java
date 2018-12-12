/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.util.Optional;

public interface ScheduleExpressionParser {

    Optional<? extends ScheduleExpression> parse(String string);

}
