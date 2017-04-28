/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Installer implements FullInstaller, PrivilegesProvider {
    private final DataModel dataModel;
    private final UserService userService;
    private final EventService eventService;
    private final FiniteStateMachineService stateMachineService;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public Installer(DataModel dataModel, UserService userService, EventService eventService, FiniteStateMachineService stateMachineService, UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.eventService = eventService;
        this.stateMachineService = stateMachineService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        this.userService.addModulePrivileges(this);
        doTry(
                "Create event types for " + getModuleName(),
                this::createEventTypes,
                logger
        );
        doTry(
                "Create stage set for " + getModuleName(),
                this::installUsagePointStageSet,
                logger
        );
    }

    @Override
    public String getModuleName() {
        return UsagePointLifeCycleConfigurationService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(UsagePointLifeCycleConfigurationService.COMPONENT_NAME,
                        Privileges.RESOURCE_USAGE_POINT_LIFECYCLE.getKey(), Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.USAGE_POINT_LIFE_CYCLE_VIEW, Privileges.Constants.USAGE_POINT_LIFE_CYCLE_ADMINISTER)),
                this.userService.createModuleResourceWithPrivileges(UsagePointLifeCycleConfigurationService.COMPONENT_NAME,
                        Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS.getKey(), Privileges.RESOURCE_USAGE_POINT_LIFECYCLE_LEVELS_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.EXECUTE_TRANSITION_1, Privileges.Constants.EXECUTE_TRANSITION_2,
                                Privileges.Constants.EXECUTE_TRANSITION_3, Privileges.Constants.EXECUTE_TRANSITION_4)));
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

    private void installUsagePointStageSet() {
        StageSetBuilder stageSetBuilder = stateMachineService.newStageSet(UsagePointLifeCycleConfigurationService.USAGE_POINT_STAGE_SET_NAME);
        Stream.of(UsagePointStage.values())
                .forEach(usagePointStage -> stageSetBuilder.stage(usagePointStage.getKey()));
        stageSetBuilder.add();
    }
}
