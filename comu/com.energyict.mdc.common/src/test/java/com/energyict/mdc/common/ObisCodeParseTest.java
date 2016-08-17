/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the parsing feature of the {@link ObisCode} component.
 */
public class ObisCodeParseTest {

    @Test(expected = IllegalArgumentException.class)
    public void emptyStringFails() {
        ObisCode.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notEnoughDigitsFails() {
        ObisCode.fromString("0.6.128.0.255");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooManyDigitsFails() {
        ObisCode.fromString("0.6.128.0.255.02.05.69");
    }

    @Test(expected = IllegalArgumentException.class)
    public void largeDigitsFails() {
        ObisCode.fromString("0.6.512.0.255.1");
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
    public void wildcard() {
        ObisCode obisCode = ObisCode.fromString("11.x.97.13.103.1");

        // Asserts
        assertThat(obisCode).isNotNull();
        assertThat(obisCode.getA()).isEqualTo(11);
        assertThat(obisCode.getB()).isEqualTo(-1);
        assertThat(obisCode.getC()).isEqualTo(97);
        assertThat(obisCode.getD()).isEqualTo(13);
        assertThat(obisCode.getE()).isEqualTo(103);
        assertThat(obisCode.getF()).isEqualTo(1);
    }

}