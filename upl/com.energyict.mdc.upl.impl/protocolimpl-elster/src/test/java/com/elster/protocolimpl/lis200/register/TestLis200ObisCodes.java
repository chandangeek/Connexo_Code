package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.registers.Lis200ObisCode;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * User: heuckeg
 * Date: 05.04.11
 * Time: 16:06
 */
public class TestLis200ObisCodes {

    @Test
    public void obisCodeMatchTest() {

        assertEquals(false, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.0"));
        assertEquals(true, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.1"));
        assertEquals(true, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.9"));
        assertEquals(true, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.10"));
        assertEquals(true, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.15"));
        assertEquals(false, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.16"));
        assertEquals(false, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.99"));
        assertEquals(false, Lis200ObisCode.MAIN_COUNTER_HIST.matches("7.0.23.2.0.255"));

    }
}
