package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntry;

public interface TaskLogEntry extends LogEntry {
    TaskOccurrence getTaskOccurrence();
}
