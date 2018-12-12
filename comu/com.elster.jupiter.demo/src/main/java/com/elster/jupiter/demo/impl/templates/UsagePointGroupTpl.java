/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.UsagePointGroupBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;

import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.Set;

public enum UsagePointGroupTpl implements Template<UsagePointGroup, UsagePointGroupBuilder> {
    RESIDENTIAL_ELECTRICITY("Residential electricity", null, EnumSet.of(ServiceKind.ELECTRICITY), ImmutableSet.of(DefaultState.ACTIVE.getKey())),
    RESIDENTIAL_GAS("Residential gas", null, EnumSet.of(ServiceKind.GAS), ImmutableSet.of(DefaultState.ACTIVE.getKey())),
    RESIDENTIAL_WATER("Residential water", null, EnumSet.of(ServiceKind.WATER), ImmutableSet.of(DefaultState.ACTIVE.getKey()));

    private String name;
    private String namePrefix;
    private Set<ServiceKind> serviceKinds;
    private Set<String> stateNames;

    UsagePointGroupTpl(String name, String namePrefix, Set<ServiceKind> serviceKinds, Set<String> stateNames) {
        this.name = name;
        this.namePrefix = namePrefix;
        this.serviceKinds = serviceKinds;
        this.stateNames = stateNames;
    }

    @Override
    public Class<UsagePointGroupBuilder> getBuilderClass() {
        return UsagePointGroupBuilder.class;
    }

    @Override
    public UsagePointGroupBuilder get(UsagePointGroupBuilder builder) {
        return builder.withName(this.name)
                .withNamePrefix(namePrefix)
                .ofServiceKinds(serviceKinds)
                .inStates(stateNames);
    }

    public String getName() {
        return name;
    }
}
