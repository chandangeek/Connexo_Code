package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.Arrays;
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
        return excludedStates;
    }

    public void setExcludedStates(String... excludedStates) {
        this.excludedStates.addAll(Arrays.asList(excludedStates));
    }
}
