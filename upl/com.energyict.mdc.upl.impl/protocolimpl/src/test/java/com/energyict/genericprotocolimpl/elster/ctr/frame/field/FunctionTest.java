package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 18:36:59
 */
public class FunctionTest {

    @Test
    public void testForDuplicates() throws Exception {
        for (Function function : Function.values()) {
            int count = 0;
            for (Function otherFunction : Function.values()) {
                count += (function.getFunctionCode() == otherFunction.getFunctionCode()) ? 1 : 0;
            }
            assertEquals("Function code must be unique in Enum Function [" + function + "]", 1, count);
        }
    }

}
