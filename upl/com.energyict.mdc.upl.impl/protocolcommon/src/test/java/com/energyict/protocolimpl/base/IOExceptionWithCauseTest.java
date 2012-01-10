package com.energyict.protocolimpl.base;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Copyrights EnergyICT
 * Date: 10/01/12
 * Time: 8:47
 */
public class IOExceptionWithCauseTest {

    @Test
    public void testException() throws Exception {
        IOExceptionWithCause nullCause = new IOExceptionWithCause(null);
        assertNull(nullCause.getMessage());
        assertNull(nullCause.getCause());
        assertNotNull(nullCause.getStackTrace());

        IOExceptionWithCause nullCauseAndMessage = new IOExceptionWithCause(null, null);
        assertNull(nullCauseAndMessage.getMessage());
        assertNull(nullCauseAndMessage.getCause());
        assertNotNull(nullCauseAndMessage.getStackTrace());

        IOExceptionWithCause nullMessage = new IOExceptionWithCause(null, nullCause);
        assertNull(nullMessage.getMessage());
        assertNotNull(nullMessage.getCause());
        assertNotNull(nullMessage.getStackTrace());

        IOExceptionWithCause messageAndCause = new IOExceptionWithCause("Test message", nullCause);
        assertNotNull(messageAndCause.getMessage());
        assertNotNull(messageAndCause.getCause());
        assertNotNull(messageAndCause.getStackTrace());
    }
}
