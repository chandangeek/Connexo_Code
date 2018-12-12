/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.exception;

import org.junit.Test;
import org.mockito.InOrder;

import java.sql.SQLException;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class ExceptionCatcherTest {

    private static interface Verifier {
        void run1();
        void run2();
        void run3();
    }

    @Test
    public void testNoExceptionsSimplyRunsAll() {
        Verifier verifier = mock(Verifier.class);

        ExceptionCatcher.executing(verifier::run1, verifier::run2, verifier::run3).andHandleExceptionsWith(e -> {
        }).execute();

        InOrder inOrder = inOrder(verifier);
        inOrder.verify(verifier).run1();
        inOrder.verify(verifier).run2();
        inOrder.verify(verifier).run3();
    }

    @Test
    public void testSecondThrowsExceptionStillRunsAll() {
        Verifier verifier = mock(Verifier.class);
        doThrow(SQLException.class).when(verifier).run2();

        ExceptionCatcher.executing(verifier::run1, verifier::run2, verifier::run3).andHandleExceptionsWith(e -> {
        }).execute();

        InOrder inOrder = inOrder(verifier);
        inOrder.verify(verifier).run1();
        inOrder.verify(verifier).run2();
        inOrder.verify(verifier).run3();
    }

    @Test
    public void testSecondThrowsExceptionTriggersHandler() {
        Verifier verifier = mock(Verifier.class);
        RuntimeException toBeThrown = new RuntimeException();
        doThrow(toBeThrown).when(verifier).run2();

        Consumer<Exception> handler = mock(Consumer.class);

        ExceptionCatcher.executing(verifier::run1, verifier::run2, verifier::run3).andHandleExceptionsWith(handler).execute();

        InOrder inOrder = inOrder(verifier, handler);
        inOrder.verify(verifier).run1();
        inOrder.verify(verifier).run2();
        inOrder.verify(handler).accept(toBeThrown);
        inOrder.verify(verifier).run3();

    }


}