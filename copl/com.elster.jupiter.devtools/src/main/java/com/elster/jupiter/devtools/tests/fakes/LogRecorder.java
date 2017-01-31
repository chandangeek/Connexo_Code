/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.fakes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * This class is actually an implementation of {@link java.util.logging.Handler}.
 * It will simply record all {@link java.util.logging.LogRecord}s that it receives in a list.
 * Its purpose is for unit tests to be able to make asserts and verify interactions with it.
 *
 * @author Tom De Greyt (tgr)
 */
public class LogRecorder extends Handler {

    private List<LogRecord> records = new ArrayList<LogRecord>();
    private Level level;

    /**
     * Create a new LogRecorder instance for the given {@link java.util.logging.Level}.
     * This instance will record all {@link java.util.logging.LogRecord}s of the given {@link java.util.logging.Level} or higher.
     * @param level
     */
    public LogRecorder(Level level) {
        this.level = level;
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() >= level.intValue()) {
            records.add(record);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        records.clear();
    }

    /**
     * @return an unmodiafiable {@link java.util.List} containing the recorded {@link java.util.logging.LogRecord}s.
     */
    public List<LogRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * @return a {@link java.util.List} containing the recorded {@link java.util.logging.LogRecord}s of the specified {@link java.util.logging.Level}.
     */
    public List<LogRecord> getRecords(Level level) {
        return records.stream()
                .filter(record -> record.getLevel().equals(level))
                .collect(Collectors.toList());
    }
}
