/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link NumberParser} component.
 */
public class NumberParserTest {

    @Test
    public void validPositiveIntegerInput() {
        Number parsed = this.getTestInstance().parse("97");

        // Asserts
        assertThat(parsed).isEqualTo(new Long(97));
    }

    @Test
    public void validNegativeIntegerInput() {
        Number parsed = this.getTestInstance().parse("-97");

        // Asserts
        assertThat(parsed).isEqualTo(new Long(-97));
    }

    @Test
    public void validPositiveDecimalInput() {
        Number parsed = this.getTestInstance().parse("97.13");

        // Asserts
        assertThat(parsed).isEqualTo(new Double(97.13));
    }

    @Test
    public void validNegativeDecimalInput() {
        Number parsed = this.getTestInstance().parse("-97.13");

        // Asserts
        assertThat(parsed).isEqualTo(new Double(-97.13));
    }

    @Test(expected = ValueParserException.class)
    public void packageVersionInput() {
        this.getTestInstance().parse("1.10.6");

        // Asserts: see expected exception rule
    }

    @Test(expected = ValueParserException.class)
    public void textOnly() {
        this.getTestInstance().parse("Hello world");

        // Asserts: see expected exception rule
    }

    @Test(expected = ValueParserException.class)
    public void textAndSomeDigits() {
        this.getTestInstance().parse("Hello2016");

        // Asserts: see expected exception rule
    }

    @Test(expected = ValueParserException.class)
    public void dateAndTimeInput() {
        this.getTestInstance().parse("21/01/2010 00:22");

        // Asserts: see expected exception rule
    }

    @Test(expected = ValueParserException.class)
    public void dateOnlyInput() {
        this.getTestInstance().parse("21/01/2010");

        // Asserts: see expected exception rule
    }

    private NumberParser getTestInstance() {
        return new NumberParser();
    }

}