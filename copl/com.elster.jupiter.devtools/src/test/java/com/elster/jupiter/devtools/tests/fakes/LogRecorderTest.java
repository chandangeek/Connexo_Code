/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests.fakes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tom De Greyt (tgr)
 */
public class LogRecorderTest {

    private static final String MESSAGE = "msg";
    private Logger logger;
    private LogRecorder recorder;

    @Before
    public void setUp() {
        logger = Logger.getLogger(LogRecorderTest.class.getName());
        Arrays.stream(logger.getHandlers())
                .forEach(logger::removeHandler);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetRecordsDoesNotRecordLowerLevels() throws Exception {
        recorder = new LogRecorder(Level.WARNING);
        logger.addHandler(recorder);

        logger.log(Level.INFO, MESSAGE);

        assertThat(recorder.getRecords()).isNotNull().isEmpty();
    }

    @Test
    public void testGetRecordsDoesRecordEqualLevels() throws Exception {
        recorder = new LogRecorder(Level.WARNING);
        logger.addHandler(recorder);

        logger.log(Level.WARNING, MESSAGE);

        assertThat(recorder.getRecords()).isNotNull().isNotEmpty();
        LogRecord logRecord = recorder.getRecords().get(0);
        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    public void testGetRecordsDoesRecordHigherLevels() throws Exception {
        recorder = new LogRecorder(Level.WARNING);
        logger.addHandler(recorder);

        logger.log(Level.SEVERE, MESSAGE);

        assertThat(recorder.getRecords()).isNotNull().isNotEmpty();
        LogRecord logRecord = recorder.getRecords().get(0);
        assertThat(logRecord.getLevel()).isEqualTo(Level.SEVERE);
        assertThat(logRecord.getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    public void testCloseClearsRecords() throws Exception {
        recorder = new LogRecorder(Level.WARNING);
        logger.addHandler(recorder);

        logger.log(Level.WARNING, MESSAGE);

        assertThat(recorder.getRecords()).isNotNull().isNotEmpty();

        recorder.close();

        assertThat(recorder.getRecords()).isNotNull().isEmpty();
    }


    @Test
    public void testGetRecordsOfLevel() throws Exception {
            recorder = new LogRecorder(Level.WARNING);
            logger.addHandler(recorder);

            logger.log(Level.WARNING, MESSAGE);

            assertThat(recorder.getRecords(Level.WARNING)).isNotNull().isNotEmpty();
            LogRecord logRecord = recorder.getRecords().get(0);
            assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
            assertThat(logRecord.getMessage()).isEqualTo(MESSAGE);
    }

    @Test
    public void testGetRecordsOfHigherLevel() throws Exception {
        recorder = new LogRecorder(Level.WARNING);
        logger.addHandler(recorder);

        logger.log(Level.SEVERE, MESSAGE);

        assertThat(recorder.getRecords(Level.WARNING)).isNotNull().isEmpty();
        assertThat(recorder.getRecords(Level.SEVERE)).isNotNull().isNotEmpty();
        LogRecord logRecord = recorder.getRecords().get(0);
        assertThat(logRecord.getLevel()).isEqualTo(Level.SEVERE);
        assertThat(logRecord.getMessage()).isEqualTo(MESSAGE);
    }
}
