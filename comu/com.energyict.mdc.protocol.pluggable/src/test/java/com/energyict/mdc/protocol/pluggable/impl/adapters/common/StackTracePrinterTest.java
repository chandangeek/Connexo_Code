package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.BusinessException;
import org.junit.*;

import java.io.IOError;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link StackTracePrinter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-15 (16:23)
 */
public class StackTracePrinterTest {

    private static final String MESSAGE = "For unit testing purposes only";

    @Test
    public void testNull () {
        assertThat(StackTracePrinter.print(null)).isNull();
    }

    @Test
    public void testCheckException () {
        assertThat(StackTracePrinter.print(new Exception("For unit testing purposes only"))).isNotEmpty();
    }

    @Test
    public void testNestedException () {
        assertThat(StackTracePrinter.print(new BusinessException(new Exception("For unit testing purposes only")))).contains("Caused by: java.lang.Exception: " + MESSAGE);
    }

    @Test
    public void testRuntimeException () {
        assertThat(StackTracePrinter.print(new RuntimeException("For unit testing purposes only"))).isNotEmpty();
    }

    @Test
    public void testError () {
        assertThat(StackTracePrinter.print(new IOError(new Exception(MESSAGE)))).isNotEmpty();
    }

}