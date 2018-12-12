/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LatitudeTest {

    @Test
    public void testToStringNorth() {
        Latitude latitude = new Latitude(BigDecimal.valueOf(1456, 2));
        assertThat(latitude.toString()).isEqualTo("14\u00B033'36''N");
    }

    @Test
    public void testToStringSouth() {
        Latitude latitude = new Latitude(BigDecimal.valueOf(-1456, 2));
        assertThat(latitude.toString()).isEqualTo("14\u00B033'36''S");

    }

}
