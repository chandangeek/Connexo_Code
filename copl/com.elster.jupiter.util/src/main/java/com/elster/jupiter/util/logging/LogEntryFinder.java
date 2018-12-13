/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.logging;

import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

public interface LogEntryFinder {

    public LogEntryFinder with (Condition condition);

    public LogEntryFinder setStart (Integer start);

    public LogEntryFinder setLimit (Integer limit);

    public List<? extends LogEntry> find();
}
