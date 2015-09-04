package com.energyict.mdc.io;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 1/09/2015
 * Time: 15:05
 */
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
