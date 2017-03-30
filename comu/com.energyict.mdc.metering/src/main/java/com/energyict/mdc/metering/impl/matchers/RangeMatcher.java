/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangeMatcher implements Matcher<Integer>{

    private List<Range> ranges = new ArrayList<>();

    private RangeMatcher(Range... ranges) {
        Collections.addAll(this.ranges, ranges);
    }

    @Override
    public boolean match(Integer field) {
        for (Range range : ranges) {
            if (range.includes(field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Integer> getAllMatches() {
        List<Integer> allMatches = new ArrayList<>();
        for (Range range : ranges) {
            for (int i = range.getFrom(); i <= range.getTo(); i++) {
                allMatches.add(i);
            }
        }
        return allMatches;
    }

    public static final RangeMatcher rangeMatcherFor(Range... ranges){
        return new RangeMatcher(ranges);
    }
}
