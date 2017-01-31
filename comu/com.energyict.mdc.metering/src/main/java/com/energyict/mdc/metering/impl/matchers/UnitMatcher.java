/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import com.energyict.mdc.common.BaseUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnitMatcher implements Matcher<Integer> {

    private final Matcher<Integer> realMatcher;

    private UnitMatcher(final Matcher<Integer> realMatcher) {
        this.realMatcher = realMatcher;
    }

    @Override
    public boolean match(Integer field) {
        return realMatcher.match(field);
    }

    @Override
    public List<Integer> getAllMatches() {
        return this.realMatcher.getAllMatches();
    }

    public static final UnitMatcher flowUnitMatcher() {
        return new UnitMatcher(new FlowUnitMatcher());
    }

    public static final UnitMatcher volumeUnitMatcher() {
        return new UnitMatcher(new VolumeUnitMatcher());
    }

    /**
     * Defines a matcher to indicate if the given field is a BaseUnit which is a 'flow unit'
     * <p/>
     * Copyrights EnergyICT
     * Date: 18/12/13
     * Time: 18:09
     */
    private static class FlowUnitMatcher implements Matcher<Integer> {

        @Override
        public boolean match(Integer field) {
            return BaseUnit.get(field).isFlowUnit();
        }

        @Override
        public List<Integer> getAllMatches() {
            List<Integer> allFlowUnits = new ArrayList<>();
            Iterator<BaseUnit> baseUnitIterator = BaseUnit.iterator();
            while (baseUnitIterator.hasNext()) {
                BaseUnit baseUnit = baseUnitIterator.next();
                if (baseUnit.isFlowUnit()) {
                    allFlowUnits.add(baseUnit.getDlmsCode());
                }
            }
            return allFlowUnits;
        }
    }

    /**
     * Defines a matcher to indicate if the given field is a BaseUnit which is a 'volume unit'
     * <p/>
     * Copyrights EnergyICT
     * Date: 18/12/13
     * Time: 18:10
     */
    private static class VolumeUnitMatcher implements Matcher<Integer> {

        @Override
        public boolean match(Integer field) {
            return BaseUnit.get(field).isVolumeUnit();
        }

        @Override
        public List<Integer> getAllMatches() {
            List<Integer> allVolumeUnits = new ArrayList<>();
            Iterator<BaseUnit> baseUnitIterator = BaseUnit.iterator();
            while (baseUnitIterator.hasNext()) {
                BaseUnit baseUnit = baseUnitIterator.next();
                if (baseUnit.isVolumeUnit()) {
                    allVolumeUnits.add(baseUnit.getDlmsCode());
                }
            }
            return allVolumeUnits;
        }

    }
}
