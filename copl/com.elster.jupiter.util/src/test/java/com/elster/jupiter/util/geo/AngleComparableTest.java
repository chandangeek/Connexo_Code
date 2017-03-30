/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import com.elster.jupiter.devtools.tests.ComparableContractTest;
import com.google.common.collect.ImmutableSet;

import java.math.BigDecimal;
import java.util.Set;

public class AngleComparableTest extends ComparableContractTest<Angle> {

    private static final Angle ANGLE = new Angle(BigDecimal.valueOf(1455, 2));

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected boolean isConsistentWithEquals() {
        return true;
    }

    @Override
    protected Angle getInstanceA() {
        return ANGLE;
    }

    @Override
    protected Angle getInstanceEqualToA() {
        return new Angle(BigDecimal.valueOf(14550, 3));
    }

    @Override
    protected Angle getInstanceNotEqualToAButEqualByComparisonToA() {
        return new Angle(BigDecimal.valueOf(14551, 3));
    }

    @Override
    protected Set<Angle> getInstancesEqualByComparisonToA() {
        return ImmutableSet.of(new Angle(BigDecimal.valueOf(14550, 3)));
    }

    @Override
    protected Set<Angle> getInstancesLessThanA() {
        return ImmutableSet.of(new Angle(BigDecimal.valueOf(-14550, 3)), new Angle(BigDecimal.valueOf(14549, 3)));
    }

    @Override
    protected Set<Angle> getInstancesGreaterThanA() {
        return ImmutableSet.of(new Angle(BigDecimal.valueOf(914550, 3)), new Angle(BigDecimal.valueOf(14551, 3)));
    }

    @Override
    protected Set<Angle> getInstancesOfSubclassEqualByComparisonToA() {
        return ImmutableSet.of(new Latitude(BigDecimal.valueOf(14550, 3)), new Longitude(BigDecimal.valueOf(14550, 3)));
    }

    @Override
    protected Set<Angle> getInstancesOfSubclassLessThanA() {
        return ImmutableSet.of(new Latitude(BigDecimal.valueOf(-20, 3)), new Longitude(BigDecimal.valueOf(1449, 2)));
    }

    @Override
    protected Set<Angle> getInstancesOfSubclassGreaterThanA() {
        return ImmutableSet.of(new Latitude(BigDecimal.valueOf(8000, 0)), new Longitude(BigDecimal.valueOf(1449, 0)));
    }

    @Override
    protected boolean isEqualsAndHashCodeOverridden() {
        return true;
    }


}
