package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntry;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:38
 */
public interface TaskLogEntry extends LogEntry {
    TaskOccurrence getTaskOccurrence();
}
