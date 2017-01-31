/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class RunMessageHandlerTaskAsTest {

    @Mock
    private MessageHandlerTask task;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private Principal principal;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRunIsEncapsulatedBySettingAndClearingPrincipal() {

        RunMessageHandlerTaskAs taskAs = new RunMessageHandlerTaskAs(task, threadPrincipalService, principal);
        taskAs.run();

        InOrder inOrder = inOrder(threadPrincipalService, task);

        inOrder.verify(threadPrincipalService).set(principal);
        inOrder.verify(task).run();
        inOrder.verify(threadPrincipalService).clear();
    }

    @Test
    public void testRunIsEncapsulatedBySettingAndClearingPrincipalEvenWhenTaskThrowsException() {

        doThrow(RuntimeException.class).when(task).run();

        RunMessageHandlerTaskAs taskAs = new RunMessageHandlerTaskAs(task, threadPrincipalService, principal);
        try {
            taskAs.run();
        } catch (RuntimeException e) {
            //expected
        }

        InOrder inOrder = inOrder(threadPrincipalService, task);

        inOrder.verify(threadPrincipalService).set(principal);
        inOrder.verify(task).run();
        inOrder.verify(threadPrincipalService).clear();
    }

}
