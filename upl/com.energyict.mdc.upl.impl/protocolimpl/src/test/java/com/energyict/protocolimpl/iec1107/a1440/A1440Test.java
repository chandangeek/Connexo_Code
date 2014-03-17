package com.energyict.protocolimpl.iec1107.a1440;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.Assert.assertNotNull;

/**
 * @author jme
 * @since 19-aug-2009
 */
public class A1440Test {

    @Test
    public void testMethods() {

        Number n = 40.95687897f;
        System.out.printf("Input: %10s \tOutput: %10s\n", n.toString(), formatSignificant(123.456f, 4));


        A1440 a1440 = new A1440();
        assertNotNull(a1440.getProtocolVersion());
    }

    public static String formatSignificant(Number value, int significant) {
        MathContext mathContext = new MathContext(significant, RoundingMode.DOWN);
        BigDecimal bigDecimal = new BigDecimal(value.toString(), mathContext);
        return bigDecimal.toPlainString();
    }

}
