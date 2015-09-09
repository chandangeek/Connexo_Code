package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;

/**
 * The <i>MacroPeriod</i> or <i>Time-period of interest</i> is defined by CIM  as follow:
 * <p>
 * The time-period of interest attribute captures an aspect of the data that reflects how it is viewed or captured over a period of time.
 * </p>
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 14:57
 */
enum MacroPeriodMapping {

    BILLINGPERIOD(MacroPeriod.BILLINGPERIOD, Matcher.DONT_CARE, ItemMatcher.itemsDontMatchFor(255), ItemMatcher.itemsDontMatchFor(
            TimeDuration.days(1).getSeconds(),
            TimeDuration.weeks(1).getSeconds(),
            TimeDuration.months(1).getSeconds())),
    DAYS(MacroPeriod.DAILY, RangeMatcher.rangeMatcherFor(new Range(1, 63)), Matcher.DONT_CARE,
            ItemMatcher.itemMatcherFor(TimeDuration.days(1).getSeconds())),
    WEEKS(MacroPeriod.WEEKLYS, RangeMatcher.rangeMatcherFor(new Range(0, 63)), Matcher.DONT_CARE,
            ItemMatcher.itemMatcherFor(TimeDuration.weeks(1).getSeconds())),
    MONTHS(MacroPeriod.MONTHLY, RangeMatcher.rangeMatcherFor(new Range(0, 63)), ItemMatcher.itemMatcherFor(255),
            ItemMatcher.itemMatcherFor(TimeDuration.months(1).getSeconds())),
    NOTAPPLICABLE(MacroPeriod.NOTAPPLICABLE, Matcher.DONT_CARE, ItemMatcher.itemMatcherFor(255), Matcher.DONT_CARE);

    private final MacroPeriod macroPeriod;
    private final Matcher<Integer> eFieldMatcher;
    private final Matcher<Integer> fFieldMatcher;
    private final Matcher<Integer> timeDurationMatcher;

    MacroPeriodMapping(MacroPeriod macroPeriod, Matcher<Integer> eFieldMatcher, Matcher<Integer> fFieldMatcher, Matcher<Integer> timeDuration) {
        this.timeDurationMatcher = timeDuration;
        this.macroPeriod = macroPeriod;
        this.eFieldMatcher = eFieldMatcher;
        this.fFieldMatcher = fFieldMatcher;
    }

    public static MacroPeriod getMacroPeriodFor(ObisCode obisCode, TimeDuration timeDuration) {
        if (timeDuration == null) {
            timeDuration = new TimeDuration(0);
        }
        if (obisCode != null) {
            for (MacroPeriodMapping macroPeriodMapping : values()) {
                if(macroPeriodMapping.timeDurationMatcher.match(timeDuration.getSeconds())
                        && macroPeriodMapping.eFieldMatcher.match(obisCode.getE())
                        && macroPeriodMapping.fFieldMatcher.match(obisCode.getF())){
                    return macroPeriodMapping.macroPeriod;
                }
            }
        }
        return MacroPeriod.NOTAPPLICABLE;
    }

    Matcher<Integer> getTimeDurationMatcher() {
        return timeDurationMatcher;
    }

    MacroPeriod getMacroPeriod() {
        return macroPeriod;
    }

    Matcher<Integer> geteFieldMatcher() {
        return eFieldMatcher;
    }

    Matcher<Integer> getfFieldMatcher() {
        return fFieldMatcher;
    }

}