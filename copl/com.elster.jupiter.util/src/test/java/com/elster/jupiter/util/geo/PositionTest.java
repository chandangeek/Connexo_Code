/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PositionTest extends EqualsContractTest {

    //40° 42' 51" N / 74° 0' 23" W
    private static final Position NEW_YORK = new Position(new BigDecimal("40.714166666667"), new BigDecimal("-74.0063888888889"));
    //26° 12' 0" S / 28° 5' 0" E
    private static final Position JOHANNESBURG = new Position(new BigDecimal("-26.200000000000"), new BigDecimal("28.0833333333333"));

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceA() {
        return NEW_YORK;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new Position(new BigDecimal("40.714166666667"), new BigDecimal("-74.0063888888889"));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(JOHANNESBURG);
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testDistance() {
        assertThat(JOHANNESBURG.distance(NEW_YORK)).isEqualTo(12842458.522575261d);
    }

    @Test
    public void testToString() {
        assertThat(NEW_YORK.toString()).isEqualTo("40\u00B042'51''N 74\u00B00'23''W");
    }

    @Test
    public void testGetLatitude() {
        assertThat(JOHANNESBURG.getLatitude()).isEqualTo(new Latitude(new BigDecimal("-26.2")));
    }

    @Test
    public void testGetLongitude() {
        assertThat(JOHANNESBURG.getLongitude()).isEqualTo(new Longitude(new BigDecimal("28.0833333333333")));
    }

}
