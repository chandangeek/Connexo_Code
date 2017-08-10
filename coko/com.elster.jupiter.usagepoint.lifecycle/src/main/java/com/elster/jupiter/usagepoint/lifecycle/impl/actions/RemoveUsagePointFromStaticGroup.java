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
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public boolean isAvailableByDefault(State fromState, State toState) {
        if ((fromState.getName().equals(DefaultState.ACTIVE.getTranslation().getKey()) && toState.getName()
                .equals(DefaultState.DEMOLISHED.getTranslation().getKey())) ||
                (fromState.getName().equals(DefaultState.INACTIVE.getTranslation().getKey()) && toState.getName()
                        .equals(DefaultState.DEMOLISHED.getTranslation().getKey())))
            return true;
        else
            return false;
    }


}
