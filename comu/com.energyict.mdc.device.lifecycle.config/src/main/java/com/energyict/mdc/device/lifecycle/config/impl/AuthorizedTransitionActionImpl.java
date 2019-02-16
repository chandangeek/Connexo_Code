/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizedTransitionActionImpl extends AuthorizedActionImpl implements AuthorizedTransitionAction {

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", groups = {Save.Create.class, Save.Update.class})
    private Reference<StateTransition> stateTransition = ValueReference.absent();
    @SuppressWarnings("unused")
    private List<DeviceAuthorizedActionMicroCheckUsageImpl> microCheckUsages = new ArrayList<>();
    @SuppressWarnings("unused")
    private long actionBits;
    private EnumSet<MicroAction> actions = EnumSet.noneOf(MicroAction.class);

    private final Thesaurus thesaurus;
    private final DataModel dataModel;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public AuthorizedTransitionActionImpl(DataModel dataModel, Thesaurus thesaurus,
                                          DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super(dataModel);
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public AuthorizedTransitionActionImpl initialize(DeviceLifeCycleImpl deviceLifeCycle, StateTransition stateTransition) {
        this.setDeviceLifeCycle(deviceLifeCycle);
        this.stateTransition.set(stateTransition);
        return this;
    }

    @Override
    public void postLoad() {
        super.postLoad();
        this.postLoadActionsEnumSet();
    }

    private void postLoadActionsEnumSet() {
        int mask = 1;
        for (MicroAction microAction : MicroAction.values()) {
            if ((this.actionBits & mask) != 0) {
                // The bit corresponding to the current microAction is set so add it to the set.
                this.actions.add(microAction);
            }
            mask = mask * 2;
        }
    }

    @Override
    public String getName() {
        return getStateTransition().getName(this.thesaurus);
    }

    @Override
    public StateTransition getStateTransition() {
        return this.stateTransition.get();
    }

    @Override
    public State getState() {
        return this.getStateTransition().getFrom();
    }

    @Override
    public Set<MicroCheckNew> getChecks() {
        return DecoratedStream.decorate(this.microCheckUsages.stream())
                .distinct(DeviceAuthorizedActionMicroCheckUsageImpl::getKey)
                .map(DeviceAuthorizedActionMicroCheckUsageImpl::getCheck)
                .collect(Collectors.toSet());
    }

    void clearChecks() {
        this.microCheckUsages.clear();
    }

    void add(Set<String> microCheckKeys) {
        this.microCheckUsages.clear();
        if (microCheckKeys != null) {
            microCheckKeys.stream()
                    .map(this.deviceLifeCycleConfigurationService::getMicroCheckByKey)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(check -> this.dataModel.getInstance(DeviceAuthorizedActionMicroCheckUsageImpl.class).init(this, check))
                    .forEach(this.microCheckUsages::add);
        }
    }

    @Override
    public Set<MicroAction> getActions() {
        return EnumSet.copyOf(this.actions);
    }

    void clearActions() {
        this.actionBits = 0;
        this.actions = EnumSet.noneOf(MicroAction.class);
    }

    void add(MicroAction action) {
        this.actionBits |= (1L << action.ordinal());
        this.actions.add(action);
    }
}