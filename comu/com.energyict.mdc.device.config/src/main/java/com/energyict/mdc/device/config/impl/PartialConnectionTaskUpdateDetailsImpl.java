package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link PartialConnectionTaskUpdateDetails} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-17 (09:16)
 */
public class PartialConnectionTaskUpdateDetailsImpl implements PartialConnectionTaskUpdateDetails {

    private final PartialConnectionTask partialConnectionTask;
    private final List<String> removedRequiredProperties;

    public PartialConnectionTaskUpdateDetailsImpl(PartialConnectionTask partialConnectionTask, List<String> removedRequiredProperties) {
        super();
        this.partialConnectionTask = partialConnectionTask;
        this.removedRequiredProperties = new ArrayList<>(removedRequiredProperties);
    }

    @Override
    public long getId() {
        return this.partialConnectionTask.getId();
    }

    @Override
    public PartialConnectionTask getPartialConnectionTask() {
        return this.partialConnectionTask;
    }

    @Override
    public List<String> getRemovedRequiredProperties() {
        return ImmutableList.copyOf(this.removedRequiredProperties);
    }

    @Override
    public String getRemovedRequiredPropertiesAsString() {
        return this.removedRequiredProperties.stream().collect(Collectors.joining(","));
    }

}