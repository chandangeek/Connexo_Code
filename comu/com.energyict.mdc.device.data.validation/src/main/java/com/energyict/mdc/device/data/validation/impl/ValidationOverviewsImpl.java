/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.validation.ValidationOverview;
import com.energyict.mdc.device.data.validation.ValidationOverviews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ValidationOverviews} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-08 (14:13)
 */
class ValidationOverviewsImpl implements ValidationOverviews {
    private Map<EndDeviceGroup, List<ValidationOverview>> overviewsByGroup = new HashMap<>();

    void add(ValidationOverviewImpl overview) {
        List<ValidationOverview> singleton = new ArrayList<>(1);
        singleton.add(overview);
        this.overviewsByGroup.merge(
                overview.getDeviceGroup(),
                singleton,
                this.merger());
    }

    private BiFunction<List<ValidationOverview>, List<ValidationOverview>, List<ValidationOverview>> merger() {
        return (c1, c2) -> {
            List<ValidationOverview> merged = new ArrayList<>(c1);
            merged.addAll(c2);
            return merged;
        };
    }

    @Override
    public List<EndDeviceGroup> getGroups() {
        return new ArrayList<>(this.overviewsByGroup.keySet());
    }

    @Override
    public List<ValidationOverview> getDeviceOverviews(EndDeviceGroup group) {
        return Optional
                .of(this.overviewsByGroup.get(group))
                .orElseThrow(() ->  // Told you so
                        new IllegalArgumentException("Device Group is not one that was returned previously by getGroups"));
    }

    @Override
    public List<ValidationOverview> allOverviews() {
        return this.getGroups()
                .stream()
                .map(this::getDeviceOverviews)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}