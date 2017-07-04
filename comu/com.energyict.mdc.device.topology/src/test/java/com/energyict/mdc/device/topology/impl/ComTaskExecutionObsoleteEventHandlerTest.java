/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 10:25
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionObsoleteEventHandlerTest {

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    Thesaurus thesaurus;
    @Mock
    ServerTopologyService topologyService;
    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    ConnectionTask connectionTask;
    @Mock
    ConnectionFunction connectionFunction;
    @Mock
    Device device;
    @Mock
    ComServer comServer;
    @Mock
    LocalEvent event;

    ComTaskExecutionObsoleteEventHandler obsoleteEventHandler;

    @Before
    public void setUp() throws Exception {
        obsoleteEventHandler = new ComTaskExecutionObsoleteEventHandler(topologyService, thesaurus);

        when(comServer.getName()).thenReturn("MyComserver");
        when(comTaskExecution.getId()).thenReturn(1L);
        when(device.getName()).thenReturn("MyDevice");
        when(comTaskExecution.getDevice()).thenReturn(device);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Test
    public void testHandleWhenUsingSpecificConnectionTask() throws Exception {
        when(comTaskExecution.usesDefaultConnectionTask()).thenReturn(false);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.empty());
        when(event.getSource()).thenReturn(comTaskExecution);

        // Business method
        obsoleteEventHandler.handle(event);

        // Asserts
        verify(topologyService, never()).findDefaultConnectionTaskForTopology(any(Device.class));
        verify(topologyService, never()).findConnectionTaskWithConnectionFunctionForTopology(any(Device.class), any(ConnectionFunction.class));
    }

    @Test
    public void testHandleWhenUsingDefaultConnectionTaskNotInUse() throws Exception {
        when(comTaskExecution.usesDefaultConnectionTask()).thenReturn(true);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.empty());
        when(event.getSource()).thenReturn(comTaskExecution);

        when(connectionTask.getExecutingComServer()).thenReturn(null);
        when(topologyService.findDefaultConnectionTaskForTopology(device)).thenReturn(Optional.of(connectionTask));

        // Business method
        obsoleteEventHandler.handle(event);

        // Asserts
        verify(topologyService).findDefaultConnectionTaskForTopology(any(Device.class));
        verify(topologyService, never()).findConnectionTaskWithConnectionFunctionForTopology(any(Device.class), any(ConnectionFunction.class));
    }

    @Test
    @Expected(
            value = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class,
            message = "You can not make comtaskexecution 1 for device MyDevice obsolete because it is currently executing on comserver MyComserver")
    public void testHandleWhenUsingDefaultConnectionTaskInUse() throws Exception {
        when(comTaskExecution.usesDefaultConnectionTask()).thenReturn(true);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.empty());
        when(event.getSource()).thenReturn(comTaskExecution);

        when(connectionTask.getExecutingComServer()).thenReturn(comServer);
        when(topologyService.findDefaultConnectionTaskForTopology(device)).thenReturn(Optional.of(connectionTask));

        // Business method
        obsoleteEventHandler.handle(event);
    }

    @Test
    public void testHandleWhenUsingConnectionTaskBasedOnConnectionFunctionNotInUse() throws Exception {
        when(comTaskExecution.usesDefaultConnectionTask()).thenReturn(false);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
        when(event.getSource()).thenReturn(comTaskExecution);

        when(connectionTask.getExecutingComServer()).thenReturn(null);
        when(topologyService.findDefaultConnectionTaskForTopology(device)).thenReturn(Optional.empty());
        when(topologyService.findConnectionTaskWithConnectionFunctionForTopology(device, connectionFunction)).thenReturn(Optional.of(connectionTask));

        // Business method
        obsoleteEventHandler.handle(event);

        // Asserts
        verify(topologyService).findDefaultConnectionTaskForTopology(any(Device.class));
        verify(topologyService).findConnectionTaskWithConnectionFunctionForTopology(any(Device.class), any(ConnectionFunction.class));
    }

    @Test
    @Expected(
            value = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class,
            message = "You can not make comtaskexecution 1 for device MyDevice obsolete because it is currently executing on comserver MyComserver")
    public void testHandleWhenUsingConnectionTaskBasedOnConnectionFunctionInUse() throws Exception {
        when(comTaskExecution.usesDefaultConnectionTask()).thenReturn(false);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
        when(event.getSource()).thenReturn(comTaskExecution);

        when(connectionTask.getExecutingComServer()).thenReturn(comServer);
        when(topologyService.findDefaultConnectionTaskForTopology(device)).thenReturn(Optional.empty());
        when(topologyService.findConnectionTaskWithConnectionFunctionForTopology(device, connectionFunction)).thenReturn(Optional.of(connectionTask));

        // Business method
        obsoleteEventHandler.handle(event);
    }
}