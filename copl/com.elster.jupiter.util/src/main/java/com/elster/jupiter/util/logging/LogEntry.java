/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.logging;

import java.time.Instant;
import java.util.logging.Level;

public interface LogEntry {
    Instant getTimestamp();

    Level getLogLevel();

    String getMessage();
}
