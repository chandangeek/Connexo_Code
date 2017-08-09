/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.TranslatableAction;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;

public class RemoveUsagePointFromStaticGroup extends TranslatableAction {

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public RemoveUsagePointFromStaticGroup(MeteringService meteringService, MeteringGroupsService meteringGroupsService) {
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    protected void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {
        this.findUsagePoint(usagePoint).ifPresent(this::execute);
    }

    private void execute(UsagePoint usagePoint) {
        this.meteringGroupsService
                .findEnumeratedUsagePointGroupsContaining(usagePoint)
                .forEach(group -> this.removeUsagePointFromGroup(group, usagePoint));
    }

    private void removeUsagePointFromGroup(EnumeratedUsagePointGroup group, UsagePoint usagePoint) {
        group
                .getEntries()
                .stream()
                .filter(each -> each.getMember().getId() == usagePoint.getId())
                .findFirst()
                .ifPresent(group::remove);
    }


    private Optional<UsagePoint> findUsagePoint(UsagePoint usagePoint) {
        return meteringService.findUsagePointByMRID(usagePoint.getMRID());
    }

    @Override
    public String getCategory() {
        return MicroCategory.REMOVE.name();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public boolean isVisibleByDefault(State fromState, State toState) {
        if((fromState.getName().equals(DefaultState.ACTIVE) && toState.getName().equals(DefaultState.DEMOLISHED)) ||
                (fromState.getName().equals(DefaultState.INACTIVE) && toState.getName().equals(DefaultState.DEMOLISHED)))
            return true;
        else
            return false;
    }

    @Override
    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.of( DefaultTransition.ACTIVATE, DefaultTransition.DEACTIVATE, DefaultTransition.INSTALL_ACTIVE, DefaultTransition.INSTALL_INACTIVE);
    }

}
