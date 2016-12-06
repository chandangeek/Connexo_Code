package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;

/**
 * The <i>Aggregate</i> or <i>Data qualifier</i> is defined by CIM as follow:
 * <p>
 * The data type can be used to describe a salient attribute of the data.
 * This attribute may be used to define a mathematical operation carried out over the period of interest defined by {@link com.elster.jupiter.cbo.MacroPeriod}.
 * </p>
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 14:59
 */
enum AggregateMapping {

    AVERAGE(Aggregate.AVERAGE,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(4, 5, 14, 15, 24, 25, 27, 28)),
    EXCESS(Aggregate.EXCESS,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(34, 38)),
    HIGHTHRESHOLD(Aggregate.HIGHTHRESHOLD,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(35)),
    LOWTHRESHOLD(Aggregate.LOWTHRESHOLD,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(31)),
    MAXIMUM(Aggregate.MAXIMUM,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(2, 6, 12, 16, 22, 26, 53, 54)),
    MINIMUM(Aggregate.MINIMUM,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(1, 3, 11, 13, 21, 23, 51, 52)),;
    private final Aggregate aggregate;
    private final Matcher<Integer> cFieldMatcher;
    private final Matcher<Integer> dFieldMatcher;

    AggregateMapping(Aggregate aggregate, Matcher<Integer> cFieldMatcher, Matcher<Integer> dFieldMatcher) {
        this.aggregate = aggregate;
        this.cFieldMatcher = cFieldMatcher;
        this.dFieldMatcher = dFieldMatcher;
    }

    public static Aggregate getAggregateFor(ObisCode obisCode, MacroPeriod macroPeriod) {
        if (obisCode != null && ObisCodeUtil.isElectricity(obisCode)) {
            for (AggregateMapping aggregateMapping : values()) {
                if (aggregateMapping.cFieldMatcher.match(obisCode.getC()) &&
                        aggregateMapping.dFieldMatcher.match(obisCode.getD())) {
                    return aggregateMapping.aggregate;
                }
            }
        }
        return Aggregate.NOTAPPLICABLE;
    }

    Aggregate getAggregate() {
        return aggregate;
    }

    Matcher<Integer> getcFieldMatcher() {
        return cFieldMatcher;
    }

    Matcher<Integer> getdFieldMatcher() {
        return dFieldMatcher;
    }
}
