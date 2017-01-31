/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.util.logging.LogEntry;

public interface TaskLogEntry extends LogEntry {
    TaskOccurrence getTaskOccurrence();
}
