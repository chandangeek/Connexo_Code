package com.elster.jupiter.util.geo;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class LongitudeTest {

    @Test
    public void testToStringEast() {
        Longitude longitude = new Longitude(BigDecimal.valueOf(1456, 2));
        assertThat(longitude.toString()).isEqualTo("14\u00B033'36\"E");
    }

    @Test
    public void testToStringWest() {
        Longitude longitude = new Longitude(BigDecimal.valueOf(-1456, 2));
        assertThat(longitude.toString()).isEqualTo("14\u00B033'36\"W");
    }

}
