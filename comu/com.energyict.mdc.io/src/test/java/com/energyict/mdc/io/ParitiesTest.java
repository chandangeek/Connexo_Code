package com.energyict.mdc.io;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 1/09/2015
 * Time: 14:53
 */
public class ParitiesTest {

    @Test
    public void getTypedValuesTest(){
        String[] typedValues = Parities.getTypedValues();

        assertThat(typedValues.length).isEqualTo(5);
        assertThat(typedValues[0]).isEqualTo(Parities.NONE.value());
        assertThat(typedValues[1]).isEqualTo(Parities.ODD.value());
        assertThat(typedValues[2]).isEqualTo(Parities.EVEN.value());
        assertThat(typedValues[3]).isEqualTo(Parities.MARK.value());
        assertThat(typedValues[4]).isEqualTo(Parities.SPACE.value());
    }

    @Test
    public void valueForValueTest(){
        assertThat(Parities.valueFor(Parities.NONE.value())).isEqualTo(Parities.NONE);
        assertThat(Parities.valueFor(Parities.ODD.value())).isEqualTo(Parities.ODD);
        assertThat(Parities.valueFor(Parities.EVEN.value())).isEqualTo(Parities.EVEN);
        assertThat(Parities.valueFor(Parities.MARK.value())).isEqualTo(Parities.MARK);
        assertThat(Parities.valueFor(Parities.SPACE.value())).isEqualTo(Parities.SPACE);
    }

    @Test
    public void valueForAbreviationTest(){
        assertThat(Parities.valueFor(Parities.NONE.getAbbreviation())).isEqualTo(Parities.NONE);
        assertThat(Parities.valueFor(Parities.ODD.getAbbreviation())).isEqualTo(Parities.ODD);
        assertThat(Parities.valueFor(Parities.EVEN.getAbbreviation())).isEqualTo(Parities.EVEN);
        assertThat(Parities.valueFor(Parities.MARK.getAbbreviation())).isEqualTo(Parities.MARK);
        assertThat(Parities.valueFor(Parities.SPACE.getAbbreviation())).isEqualTo(Parities.SPACE);
    }

}
