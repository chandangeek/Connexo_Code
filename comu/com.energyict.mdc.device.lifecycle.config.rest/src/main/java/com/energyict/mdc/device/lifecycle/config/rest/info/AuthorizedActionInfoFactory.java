/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AuthorizedActionInfoFactory {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final Thesaurus thesaurus;
    private final MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory;

    @Inject
    public AuthorizedActionInfoFactory(Thesaurus thesaurus, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, MicroActionAndCheckInfoFactory microActionAndCheckInfoFactory) {
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.microActionAndCheckInfoFactory = microActionAndCheckInfoFactory;
    }

    public AuthorizedActionInfo from(AuthorizedAction action) {
        Objects.requireNonNull(action);
        DeviceLifeCycle deviceLifeCycle = action.getDeviceLifeCycle();
        AuthorizedActionInfo info = new AuthorizedActionInfo();
        info.id = action.getId();
        info.privileges = action.getLevels().stream()
                .map(lvl -> new DeviceLifeCyclePrivilegeInfo(thesaurus, lvl))
                .collect(Collectors.toList());
        info.version = action.getVersion();
        info.parent = new VersionInfo<>(deviceLifeCycle.getId(), deviceLifeCycle.getVersion());
        if (action instanceof AuthorizedTransitionAction) {
            fromBasicAction(info, (AuthorizedTransitionAction) action);
        } else {
            fromBpmAction(info, (AuthorizedBusinessProcessAction) action);
        }
        return info;
    }

    private void fromBasicAction(AuthorizedActionInfo info, AuthorizedTransitionAction action) {
        info.name = action.getName();
        info.fromState = new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, action.getDeviceLifeCycle(), action.getStateTransition().getFrom());
        info.toState = new DeviceLifeCycleStateInfo(deviceLifeCycleConfigurationService, action.getDeviceLifeCycle(), action.getStateTransition().getTo());
        info.triggeredBy = new StateTransitionEventTypeFactory(thesaurus).from(action.getStateTransition().getEventType());
        Set<MicroAction> microActions = action.getActions();
        if (!microActions.isEmpty()) {
            info.microActions = new TreeSet<>(Comparator.<MicroActionAndCheckInfo, String>comparing(obj -> obj.category.name)
                    .thenComparing(obj -> obj.name));
            for (MicroAction microAction : microActions) {
                MicroActionAndCheckInfo microActionInfo = microActionAndCheckInfoFactory.optional(microAction);
                microActionInfo.checked = true;
                info.microActions.add(microActionInfo);
            }
        }
        Set<MicroCheck> microChecks = action.getChecks();
        if (!microChecks.isEmpty()) {
            info.microChecks = new TreeSet<>(Comparator.<MicroActionAndCheckInfo, String>comparing(obj -> obj.category.name).thenComparing(obj -> obj.name));
            microChecks.forEach(microCheck -> {
                MicroActionAndCheckInfo microActionInfo = microActionAndCheckInfoFactory.optional(microCheck);
                microActionInfo.checked = true;
                info.microChecks.add(microActionInfo);
            });
        }
    }

    private void fromBpmAction(AuthorizedActionInfo info, AuthorizedBusinessProcessAction action) {
        info.name = action.getName();
    }
}
