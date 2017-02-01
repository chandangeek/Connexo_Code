/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class EndDeviceEventTypeCodeBuilderTest {

    @Test
    public void testNAAllTheWay() {
        String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.NA).toCode();
        assertThat(code).isEqualTo("0.0.0.0");
    }

    @Test
    public void testTrivial() {
        String code = EndDeviceEventTypeCodeBuilder.type(EndDeviceType.ELECTRIC_METER)
                .domain(EndDeviceDomain.CLOCK)
                .subDomain(EndDeviceSubDomain.TIME)
                .eventOrAction(EndDeviceEventOrAction.DISABLED).toCode();
        assertThat(code).isEqualTo("3.36.114.66");
    }

}
