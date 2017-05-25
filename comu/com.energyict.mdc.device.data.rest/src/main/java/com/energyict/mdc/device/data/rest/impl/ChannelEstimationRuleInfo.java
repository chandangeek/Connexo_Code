/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ChannelEstimationRuleInfo {
    public Long id;
    public Long version;
    public Long ruleId;
    public String name;
    public String estimator;
    public ReadingTypeInfo readingType;
    public boolean isActive;
    public List<OverriddenPropertyInfo> properties;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelEstimationRuleInfo info = (ChannelEstimationRuleInfo) o;
        return Objects.equals(name, info.name) &&
                Objects.equals(estimator, info.estimator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, estimator);
    }

    static ChannelEstimationRuleInfo chooseEffectiveOne(ChannelEstimationRuleInfo info1, ChannelEstimationRuleInfo info2) {
        return effectivityComparator().compare(info1, info2) >= 0 ? info1 : info2;
    }

    static Comparator<ChannelEstimationRuleInfo> effectivityComparator() {
        return Comparator.comparing(info -> info.isActive);
    }

    static Comparator<ChannelEstimationRuleInfo> defaultComparator() {
        return Comparator.<ChannelEstimationRuleInfo, Boolean>comparing(info -> !info.isActive)
                .thenComparing(info -> info.name.toLowerCase());
    }
}
