/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.logging.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.service.log.LogService;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LogConfiguratorTest {

    private static final String MY_MESSAGE = "My message";
    private LogConfigurator logConfigurator;

    @Mock
    private LogService logService;

    @Before
    public void setUp() {
        logConfigurator = new LogConfigurator();
        logConfigurator.setLogService(logService);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                logService.log(((Integer) invocation.getArguments()[0]), (String) invocation.getArguments()[1], null);
                return null;
            }
        }).when(logService).log(anyInt(), anyString());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNoPropsUsesDefaultFormat() {
        logConfigurator.activate(Collections.<String, Object>emptyMap());

        Logger.getLogger(LogConfiguratorTest.class.getName()).log(Level.SEVERE, MY_MESSAGE);

        verify(logService).log(LogService.LOG_ERROR, MY_MESSAGE, null);
    }


}
