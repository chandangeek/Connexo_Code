/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LongitudeTest {

    @Test
    public void testToStringEast() {
        Longitude longitude = new Longitude(BigDecimal.valueOf(1456, 2));
        assertThat(longitude.toString()).isEqualTo("14\u00B033'36''E");
    }

    @Test
    public void testToStringWest() {
        Longitude longitude = new Longitude(BigDecimal.valueOf(-1456, 2));
        assertThat(longitude.toString()).isEqualTo("14\u00B033'36''W");
    }

}
