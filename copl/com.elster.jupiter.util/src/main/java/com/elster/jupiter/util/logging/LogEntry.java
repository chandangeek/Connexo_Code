package com.elster.jupiter.util.logging;

import java.time.Instant;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 7/11/2014
 * Time: 13:00
 */
public interface LogEntry {
    Instant getTimestamp();

    Level getLogLevel();

    String getMessage();
}
