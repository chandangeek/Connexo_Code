package com.elster.jupiter.security.thread;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RunAsTest {

    private RunAs runAs;

    @Mock
    private Runnable runnable;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private Principal principal;
    @Mock
    private RuntimeException runtimeException;


    @Before
    public void setUp() {
        runAs = new RunAs(threadPrincipalService, principal, runnable);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testTriviallyRegistersAndClearsSecurityContext() {
        runAs.run();

        InOrder inOrder = inOrder(threadPrincipalService, runnable);

        inOrder.verify(threadPrincipalService).set(principal);
        inOrder.verify(runnable).run();
        inOrder.verify(threadPrincipalService).clear();
    }

    @Test
    public void testRegistersAndClearsSecurityContextEvenWhenDecoratedRunnableThrowsException() {
        doThrow(runtimeException).when(runnable).run();

        try {
            runAs.run();
        } catch (RuntimeException e) {
            assertThat(e).isSameAs(runtimeException);
        }

        InOrder inOrder = inOrder(threadPrincipalService, runnable);

        inOrder.verify(threadPrincipalService).set(principal);
        inOrder.verify(runnable).run();
        inOrder.verify(threadPrincipalService).clear();
    }

    @Test
    public void testRegistersModuleAndAction() {
        runAs.module("MDL").action("action");

        runAs.run();

        InOrder inOrder = inOrder(threadPrincipalService, runnable);

        inOrder.verify(threadPrincipalService).set("MDL", "action");
        inOrder.verify(runnable).run();
        inOrder.verify(threadPrincipalService).clear();

    }

}
