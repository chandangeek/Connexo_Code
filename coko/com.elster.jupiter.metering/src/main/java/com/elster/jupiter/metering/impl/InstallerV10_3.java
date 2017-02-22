/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class InstallerV10_3 implements PrivilegesProvider {

    private final UserService userService;
    private final FiniteStateMachineService stateMachineService;

    @Inject
    public InstallerV10_3(UserService userService, FiniteStateMachineService stateMachineService) {
        this.userService = userService;
        this.stateMachineService = stateMachineService;
    }

    @Override
    public String getModuleName() {
        return MeteringDataModelService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT.getKey(),
                        DefaultTranslationKey.RESOURCE_USAGE_POINT_DESCRIPTION.getKey(),
                        Collections.singletonList(Privileges.Constants.MANAGE_USAGE_POINT_ATTRIBUTES)));
        return resources;
    }

    public void installEndDeviceStageSet() {
        StageSetBuilder stageSetBuilder = stateMachineService.newStageSet(MeteringService.END_DEVICE_STAGE_SET_NAME);
        Stream.of(EndDeviceStage.values())
                .forEach(endDeviceStage -> stageSetBuilder.stage(endDeviceStage.name()));
        stageSetBuilder.add();
    }
}
