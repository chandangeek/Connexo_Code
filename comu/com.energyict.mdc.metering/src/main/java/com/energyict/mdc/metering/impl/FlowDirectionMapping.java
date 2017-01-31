/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.FlowDirection;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;


enum FlowDirectionMapping {

    FORWARD(ItemMatcher.itemMatcherFor(1, 3, 9, 21, 23, 29, 41, 43, 49, 61, 63, 69), FlowDirection.FORWARD),
    REVERSE(ItemMatcher.itemMatcherFor(2, 4, 10, 22, 24, 30, 42, 44, 50, 62, 64, 70), FlowDirection.REVERSE),
    Q1(ItemMatcher.itemMatcherFor(5, 17, 25, 37, 45, 57, 65, 77), FlowDirection.Q1),
    Q2(ItemMatcher.itemMatcherFor(6, 18, 26, 38, 46, 58, 66, 78), FlowDirection.Q2),
    Q3(ItemMatcher.itemMatcherFor(7, 19, 27, 39, 47, 59, 67, 79), FlowDirection.Q3),
    Q4(ItemMatcher.itemMatcherFor(8, 20, 28, 40, 48, 60, 68, 80), FlowDirection.Q4),
    LEADING(ItemMatcher.itemMatcherFor(13, 33, 53, 73), FlowDirection.LEADING),
    LAGGING(ItemMatcher.itemMatcherFor(84, 85, 86, 87), FlowDirection.LAGGING),
    TOTAL(ItemMatcher.itemMatcherFor(15, 35, 55, 75), FlowDirection.TOTAL),
    NET(ItemMatcher.itemMatcherFor(16, 36, 56, 76), FlowDirection.NET),
    TOTALBYPHASE(ItemMatcher.itemMatcherFor(15, 35, 55, 75), FlowDirection.TOTALBYPHASE);

    private final Matcher<Integer> possibleCValues;
    private final FlowDirection flowDirection;

    FlowDirectionMapping(Matcher<Integer> possibleCValues, FlowDirection flowDirection) {
        this.possibleCValues = possibleCValues;
        this.flowDirection = flowDirection;
    }

    public static FlowDirection getFlowDirectionFor(ObisCode obisCode){
        if(obisCode.getA() == 1){
            for (FlowDirectionMapping flowDirectionMapping : values()) {
                if(flowDirectionMapping.possibleCValues.match(obisCode.getC())){
                    return flowDirectionMapping.flowDirection;
                }
            }
        }
        return FlowDirection.NOTAPPLICABLE;
    }

    Matcher<Integer> getPossibleCValues() {
        return possibleCValues;
    }

    FlowDirection getFlowDirection() {
        return flowDirection;
    }
}
