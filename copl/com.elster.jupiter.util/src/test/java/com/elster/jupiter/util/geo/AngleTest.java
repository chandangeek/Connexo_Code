/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AngleTest extends EqualsContractTest {

    private static final BigDecimal VALUE = BigDecimal.valueOf(1456, 2);
    private static final Angle ANGLE = new Angle(VALUE);
    private static final Angle NEGATIVE_ANGLE = new Angle(BigDecimal.valueOf(-1456, 2));

    @Override
    protected Angle getInstanceA() {
        return ANGLE;
    }

    @Override
    protected Angle getInstanceEqualToA() {
        return new Angle(BigDecimal.valueOf(14560, 3));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(getInstanceNotEqualToA());
    }

    private Angle getInstanceNotEqualToA() {
        return new Angle(BigDecimal.valueOf(1457, 2));
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return new Angle(BigDecimal.valueOf(1456, 2)) {};
    }

    @Test
    public void testCompareToEqual() {
        assertThat(ANGLE.compareTo(getInstanceEqualToA())).isEqualTo(0);
    }

    @Test
    public void testCompareToSmaller() {
        assertThat(ANGLE.compareTo(getInstanceNotEqualToA())).isLessThan(0);
    }

    @Test
    public void testCompareToGreater() {
        assertThat(getInstanceNotEqualToA().compareTo(ANGLE)).isGreaterThan(0);
    }

    @Test
    public void testGetValue() {
        assertThat(ANGLE.getValue()).isEqualTo(VALUE);
    }

    @Test
    public void testGetDegrees() {
        assertThat(ANGLE.getDegrees()).isEqualTo(14);
    }

    @Test
    public void testGetDegreesOfNegative() {
        assertThat(NEGATIVE_ANGLE.getDegrees()).isEqualTo(14);
    }

    @Test
    public void testGetMinutes() {
        assertThat(ANGLE.getMinutes()).isEqualTo(33);
    }

    @Test
    public void testGetMinutesOfNegative() {
        assertThat(NEGATIVE_ANGLE.getMinutes()).isEqualTo(33);
    }

    @Test
    public void testGetSeconds() {
        assertThat(ANGLE.getSeconds()).isEqualTo(36);
    }

    @Test
    public void testGetSecondsOfNegative() {
        assertThat(NEGATIVE_ANGLE.getSeconds()).isEqualTo(36);
    }

    @Test
    public void testGetSecondsNear60() {
        assertThat(new Angle(BigDecimal.valueOf(14999999, 6)).getSeconds()).isEqualTo(59);
    }

    @Test
    public void testToRadians() {
        assertThat(ANGLE.toRadians()).isEqualTo(0.25411993909037438640008937589194d);
    }

    @Test
    public void testSubtract() {
        assertThat(ANGLE.subtract(getInstanceNotEqualToA())).isEqualTo(new Angle(BigDecimal.valueOf(-1, 2)));
    }

    @Test
    public void testCosine() {
        assertThat(ANGLE.cos()).isEqualTo(0.96788491225261867031597706354113d);
    }

    @Test
    public void testSine() {
        assertThat(ANGLE.sin()).isEqualTo(0.25139370842115491765239287147542d);
    }

    @Test
    public void testSignumPositive() {
        assertThat(ANGLE.signum()).isEqualTo(1);
    }

    @Test
    public void testSignumNegative() {
        assertThat(NEGATIVE_ANGLE.signum()).isEqualTo(-1);
    }

    @Test
    public void testSignumZero() {
        assertThat(new Angle(BigDecimal.ZERO).signum()).isEqualTo(0);
    }

    @Test
    public void testToString() {
        assertThat(ANGLE.toString()).isEqualTo("14\u00B033'36''");
    }

}
