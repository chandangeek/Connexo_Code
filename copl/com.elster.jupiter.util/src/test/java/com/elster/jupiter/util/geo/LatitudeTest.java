package com.elster.jupiter.util.geo;

import org.junit.Test;

import java.math.BigDecimal;

import static org.fest.assertions.api.Assertions.assertThat;

public class LatitudeTest {

    @Test
    public void testToStringNorth() {
        Latitude latitude = new Latitude(BigDecimal.valueOf(1456, 2));
        assertThat(latitude.toString()).isEqualTo("14\u00B033'36\"N");
    }

    @Test
    public void testToStringSouth() {
        Latitude latitude = new Latitude(BigDecimal.valueOf(-1456, 2));
        assertThat(latitude.toString()).isEqualTo("14\u00B033'36\"S");

    }

}
