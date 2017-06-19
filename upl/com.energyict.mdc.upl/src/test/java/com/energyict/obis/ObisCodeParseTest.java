/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.obis;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the parsing feature of the {@link ObisCode} component.
 */
public class ObisCodeParseTest {

    @Test
    public void emptyStringFails() {
        ObisCode obisCode = ObisCode.fromString("");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void notEnoughDigitsFails() {
        ObisCode obisCode = ObisCode.fromString("0.6.128.0.255");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void alphaNumericalsFails() {
        ObisCode obisCode = ObisCode.fromString("0.6.Rudi.02.05.69");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void tooManyDigitsFails() {
        ObisCode obisCode = ObisCode.fromString("0.6.128.0.255.02.05.69");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void largeDigitsFails() {
        ObisCode.fromString("0.6.512.0.255.1");
    }

    @Test
    public void testVZFails() {
        ObisCode obisCode = ObisCode.fromString("11.7.97.13.103.VZ2");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void testNoVZFails() {
        ObisCode obisCode = ObisCode.fromString("11.7.97.13.103.-1");
        assertThat(obisCode.isInvalid()).isEqualTo(true);
    }

    @Test
    public void noWildcard() {
        ObisCode obisCode = ObisCode.fromString("11.7.97.13.103.1");

        // Asserts
        assertThat(obisCode).isNotNull();
        assertThat(obisCode.getA()).isEqualTo(11);
        assertThat(obisCode.getB()).isEqualTo(7);
        assertThat(obisCode.getC()).isEqualTo(97);
        assertThat(obisCode.getD()).isEqualTo(13);
        assertThat(obisCode.getE()).isEqualTo(103);
        assertThat(obisCode.getF()).isEqualTo(1);
    }

    @Test
    public void testVZ() {
        ObisCode obisCode = ObisCode.fromString("11.7.97.13.103.VZ-1");

        // Asserts
        assertThat(obisCode).isNotNull();
        assertThat(obisCode.getA()).isEqualTo(11);
        assertThat(obisCode.getB()).isEqualTo(7);
        assertThat(obisCode.getC()).isEqualTo(97);
        assertThat(obisCode.getD()).isEqualTo(13);
        assertThat(obisCode.getE()).isEqualTo(103);
        assertThat(obisCode.getF()).isEqualTo(-1);
    }

    @Test
    public void testVZPlus() {
        ObisCode obisCode = ObisCode.fromString("11.7.97.13.103.VZ+0");

        // Asserts
        assertThat(obisCode).isNotNull();
        assertThat(obisCode.getA()).isEqualTo(11);
        assertThat(obisCode.getB()).isEqualTo(7);
        assertThat(obisCode.getC()).isEqualTo(97);
        assertThat(obisCode.getD()).isEqualTo(13);
        assertThat(obisCode.getE()).isEqualTo(103);
        assertThat(obisCode.getF()).isEqualTo(0);
    }

    @Test
    public void wildcard() {
        ObisCode obisCode = ObisCode.fromString("11.x.97.13.103.2");

        // Asserts
        assertThat(obisCode).isNotNull();
        assertThat(obisCode.getA()).isEqualTo(11);
        assertThat(obisCode.getB()).isEqualTo(-1);
        assertThat(obisCode.getC()).isEqualTo(97);
        assertThat(obisCode.getD()).isEqualTo(13);
        assertThat(obisCode.getE()).isEqualTo(103);
        assertThat(obisCode.getF()).isEqualTo(2);
    }

    @Test
    public void testTrim() {
        ObisCode code = ObisCode.fromString(" \t4 .\r2\t. 5 .\n1\r. 6 . VZ +1\n");
        assertThat(code).isNotNull();
        assertThat(code.getA()).isEqualTo(4);
        assertThat(code.getB()).isEqualTo(2);
        assertThat(code.getC()).isEqualTo(5);
        assertThat(code.getD()).isEqualTo(1);
        assertThat(code.getE()).isEqualTo(6);
        assertThat(code.getF()).isEqualTo(1);
    }
}
