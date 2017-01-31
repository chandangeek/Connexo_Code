/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Phase;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.impl.matchers.CompositeMatcher;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;

enum PhaseMapping {

    VOLTAGE_ALL_PHASES(Phase.PHASEABCN, ItemMatcher.itemMatcherFor(12, 89), Matcher.DONT_CARE, Matcher.DONT_CARE),
    CURRENT_ALL_PHASES(Phase.PHASEABC, ItemMatcher.itemMatcherFor(11,88,90), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_A_TO_N(Phase.PHASEAN, ItemMatcher.itemMatcherFor(32), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_A(Phase.PHASEA, CompositeMatcher.createMatcherFor(RangeMatcher.rangeMatcherFor(new Range(21, 31), new Range(33, 40))), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_B_TO_N(Phase.PHASEBN, ItemMatcher.itemMatcherFor(52), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_B(Phase.PHASEB, CompositeMatcher.createMatcherFor(RangeMatcher.rangeMatcherFor(new Range(41,51), new Range(53,60))), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_C_TO_N(Phase.PHASECN, ItemMatcher.itemMatcherFor(72), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_C(Phase.PHASEC, CompositeMatcher.createMatcherFor(RangeMatcher.rangeMatcherFor(new Range(61,71), new Range(73,80))), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_N(Phase.PHASEN, ItemMatcher.itemMatcherFor(91,92), Matcher.DONT_CARE, Matcher.DONT_CARE),
    PHASE_A_A(Phase.PHASEAA, ItemMatcher.itemMatcherFor(81), ItemMatcher.itemMatcherFor(7), ItemMatcher.itemMatcherFor(4)),
    PHASE_B_A(Phase.PHASEBA, ItemMatcher.itemMatcherFor(81), ItemMatcher.itemMatcherFor(7), ItemMatcher.itemMatcherFor(1,5)),
    PHASE_C_A(Phase.PHASECA, ItemMatcher.itemMatcherFor(81), ItemMatcher.itemMatcherFor(7), ItemMatcher.itemMatcherFor(2,6)),
    NOT_APPLICABLE(Phase.NOTAPPLICABLE, RangeMatcher.rangeMatcherFor(new Range(1,20)), Matcher.DONT_CARE, Matcher.DONT_CARE), // when it is not applicable, then total is assumed
    ;

    private final Phase cimPhase;
    private final Matcher<Integer> cField;
    private final Matcher<Integer> dField;
    private final Matcher<Integer> eField;

    PhaseMapping(Phase cimPhase, Matcher<Integer> cField, Matcher<Integer> dField, Matcher<Integer> eField) {
        this.cimPhase = cimPhase;
        this.cField = cField;
        this.dField = dField;
        this.eField = eField;
    }


    public static Phase getPhaseFor(ObisCode obisCode) {
        if(ObisCodeUtil.isElectricity(obisCode)){
            for (PhaseMapping phaseMapping : values()) {
                if(phaseMapping.cField.match(obisCode.getC())
                        && phaseMapping.dField.match(obisCode.getD())
                        && phaseMapping.eField.match(obisCode.getE())){
                    return phaseMapping.cimPhase;
                }
            }
        }
        return Phase.NOTAPPLICABLE;
    }

    Phase getCimPhase() {
        return cimPhase;
    }

    Matcher<Integer> getcField() {
        return cField;
    }

    Matcher<Integer> getdField() {
        return dField;
    }

    Matcher<Integer> geteField() {
        return eField;
    }
}
