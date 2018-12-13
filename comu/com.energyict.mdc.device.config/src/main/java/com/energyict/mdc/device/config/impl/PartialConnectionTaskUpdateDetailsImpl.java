/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PartialConnectionTaskUpdateDetails} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-17 (09:16)
 */
public class PartialConnectionTaskUpdateDetailsImpl implements PartialConnectionTaskUpdateDetails {

    private final PartialConnectionTask partialConnectionTask;
    private final List<String> addedOrRemovedRequiredProperties;
    private final Optional<ConnectionFunction> previousConnectionFunction;

    public PartialConnectionTaskUpdateDetailsImpl(PartialConnectionTask partialConnectionTask, List<String> addedOrRemovedRequiredProperties, Optional<ConnectionFunction> previousConnectionFunction) {
        super();
        this.partialConnectionTask = partialConnectionTask;
        this.addedOrRemovedRequiredProperties = new ArrayList<>(addedOrRemovedRequiredProperties);
        this.previousConnectionFunction = previousConnectionFunction;

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
    public List<String> getAddedOrRemovedRequiredProperties() {
        return ImmutableList.copyOf(this.addedOrRemovedRequiredProperties);
    }

    @Override
    public Optional<ConnectionFunction> getPreviousConnectionFunction() {
        return previousConnectionFunction;
    }
}