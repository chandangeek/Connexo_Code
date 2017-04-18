/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class MeterFilter {

    private String name;
    private List<String> excludedStates = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getExcludedStates() {
        return this.excludedStates;
    }

    public void setExcludedStates(String... excludedStates) {
        this.excludedStates = ImmutableList.copyOf(excludedStates);
    }
}