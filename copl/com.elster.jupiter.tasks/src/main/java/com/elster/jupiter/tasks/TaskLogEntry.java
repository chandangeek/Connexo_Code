package com.elster.jupiter.tasks;

import java.time.Instant;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:38
 */
public interface TaskLogEntry {
    TaskOccurrence getTaskOccurrence();

    Instant getTimeStamp();

    String getMessage();

    Level getLevel();
}
