/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.RationalNumber;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class RationalNumberAdapterTest {

    @Test
    public void testNotApplicableMarshal() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        String marshal = adapter.marshal(RationalNumber.NOTAPPLICABLE);
        assertThat(marshal).isEqualTo(RationalNumberAdapter.NOT_APPLICABLE);
    }

    @Test
    public void testNotApplicableUnmarshal() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber marshal = adapter.unmarshal(RationalNumberAdapter.NOT_APPLICABLE);
        assertThat(marshal).isEqualTo(RationalNumber.NOTAPPLICABLE);
    }

    @Test
    public void testEmptyStringUnmarshal() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber marshal = adapter.unmarshal("");
        assertThat(marshal).isEqualTo(RationalNumber.NOTAPPLICABLE);
    }

    @Test
    public void testRationalNumberMarshal() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        String marshal = adapter.marshal(new RationalNumber(10L, 100L));
        assertThat(marshal).isEqualTo("10/100");
    }

    @Test
    public void testRationalNumberUnmarshal() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber rationalNumber = adapter.unmarshal("10/99");
        assertThat(rationalNumber).isEqualTo(new RationalNumber(10,99));
    }

    @Test
    public void testRationalNumberUnmarshalLong() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber rationalNumber = adapter.unmarshal("12345678987654321/999999999999999999");
        assertThat(rationalNumber).isEqualTo(new RationalNumber(12345678987654321L,999999999999999999L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRationalNumberUnmarshalInvalidLong() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber rationalNumber = adapter.unmarshal("1234111111115678987654321/999999999999999999");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRationalNumberUnmarshalInvalidRational() throws Exception {
        RationalNumberAdapter adapter = new RationalNumberAdapter();
        RationalNumber rationalNumber = adapter.unmarshal("123|9");
    }
}
