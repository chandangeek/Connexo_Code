package com.energyict.mdc.io;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 1/09/2015
 * Time: 14:39
 */
public class FlowControlTest {

    @Test
    public void getTypedValuesTest(){
        String[] typedValues = FlowControl.getTypedValues();

        assertThat(typedValues.length).isEqualTo(4);
        assertThat(typedValues[0]).isEqualTo(FlowControl.NONE.value());
        assertThat(typedValues[1]).isEqualTo(FlowControl.RTSCTS.value());
        assertThat(typedValues[2]).isEqualTo(FlowControl.DTRDSR.value());
        assertThat(typedValues[3]).isEqualTo(FlowControl.XONXOFF.value());
    }

    @Test
    public void valueForTest(){
        assertThat(FlowControl.valueFor(FlowControl.NONE.value())).isEqualTo(FlowControl.NONE);
        assertThat(FlowControl.valueFor(FlowControl.RTSCTS.value())).isEqualTo(FlowControl.RTSCTS);
        assertThat(FlowControl.valueFor(FlowControl.DTRDSR.value())).isEqualTo(FlowControl.DTRDSR);
        assertThat(FlowControl.valueFor(FlowControl.XONXOFF.value())).isEqualTo(FlowControl.XONXOFF);
    }

}
