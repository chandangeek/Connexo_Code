/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NrOfDataBitsTest {

    @Test
    public void getTypedValuesTest(){
        BigDecimal[] typedValues = NrOfDataBits.getTypedValues();

        assertThat(typedValues.length).isEqualTo(4);
        assertThat(typedValues[0]).isEqualTo(NrOfDataBits.FIVE.value());
        assertThat(typedValues[1]).isEqualTo(NrOfDataBits.SIX.value());
        assertThat(typedValues[2]).isEqualTo(NrOfDataBits.SEVEN.value());
        assertThat(typedValues[3]).isEqualTo(NrOfDataBits.EIGHT.value());
    }

    @Test
    public void valueForTest(){
        assertThat(NrOfDataBits.valueFor(NrOfDataBits.FIVE.value())).isEqualTo(NrOfDataBits.FIVE);
        assertThat(NrOfDataBits.valueFor(NrOfDataBits.SIX.value())).isEqualTo(NrOfDataBits.SIX);
        assertThat(NrOfDataBits.valueFor(NrOfDataBits.SEVEN.value())).isEqualTo(NrOfDataBits.SEVEN);
        assertThat(NrOfDataBits.valueFor(NrOfDataBits.EIGHT.value())).isEqualTo(NrOfDataBits.EIGHT);
    }

}
