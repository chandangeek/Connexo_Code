/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NrOfStopBitsTest {

    @Test
    public void getTypedValuesTest(){
        BigDecimal[] typedValues = NrOfStopBits.getTypedValues();

        assertThat(typedValues.length).isEqualTo(3);
        assertThat(typedValues[0]).isEqualTo(NrOfStopBits.ONE.value());
        assertThat(typedValues[1]).isEqualTo(NrOfStopBits.ONE_AND_HALF.value());
        assertThat(typedValues[2]).isEqualTo(NrOfStopBits.TWO.value());
    }

    @Test
    public void valueForTest(){
        assertThat(NrOfStopBits.valueFor(NrOfStopBits.ONE.value())).isEqualTo(NrOfStopBits.ONE);
        assertThat(NrOfStopBits.valueFor(NrOfStopBits.ONE_AND_HALF.value())).isEqualTo(NrOfStopBits.ONE_AND_HALF);
        assertThat(NrOfStopBits.valueFor(NrOfStopBits.TWO.value())).isEqualTo(NrOfStopBits.TWO);
    }

}
