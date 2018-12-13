/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.orm.Difference;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DifferencesLogListenerTest {

    @Mock
    private Difference firstDiff, secondDiff;
    private LogRecorder logRecorder;
    private Logger rootLogger;

    @Before
    public void setUp() {
        rootLogger = Logger.getLogger("");
        logRecorder = new LogRecorder(Level.ALL);
        rootLogger.addHandler(logRecorder);

        when(firstDiff.description()).thenReturn("first");
        when(secondDiff.description()).thenReturn("second");
    }

    @After
    public void tearDown() {
        rootLogger.removeHandler(logRecorder);
    }

    @Test
    public void testOnDifference() throws Exception {
        DifferencesLogListener differencesLogListener = new DifferencesLogListener();

        differencesLogListener.onDifference(firstDiff);
        differencesLogListener.onDifference(secondDiff);

        assertThat(logRecorder).hasRecordWithMessage("There are differences between DB and intended Data Model")
                .atLevel(Level.WARNING)
                .times(1);
        assertThat(logRecorder).hasRecordWithMessage("first")
                .atLevel(Level.INFO);
        assertThat(logRecorder).hasRecordWithMessage("second")
                .atLevel(Level.INFO);
    }
}