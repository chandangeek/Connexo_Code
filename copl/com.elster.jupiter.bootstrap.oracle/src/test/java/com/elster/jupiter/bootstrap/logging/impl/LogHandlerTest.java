/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.logging.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.log.LogService;

import com.elster.jupiter.bootstrap.logging.impl.LogHandler;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogHandlerTest {

    @Mock
    private LogService logService;
    @Mock
    private RuntimeException runtimeException;

    @Test
    public void testPropagationToOsgiLogService() {
        LogHandler logHandler = new LogHandler(logService, "%5$s");

        logHandler.publish(new LogRecord(Level.INFO, "message"));

        verify(logService).log(LogService.LOG_INFO, "message", null);
    }

    @Test
    public void testPropagationToOsgiLogServiceWithException() {
        LogHandler logHandler = new LogHandler(logService, "%5$s");

        LogRecord logRecord = new LogRecord(Level.INFO, "message");
        logRecord.setThrown(runtimeException);
        logHandler.publish(logRecord);

        verify(logService).log(LogService.LOG_INFO, "message", runtimeException);
    }

}

