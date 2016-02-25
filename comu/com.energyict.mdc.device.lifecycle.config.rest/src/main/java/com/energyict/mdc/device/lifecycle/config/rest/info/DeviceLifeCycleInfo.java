package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public Integer statesCount;
    public Integer actionsCount;
    public List<IdWithNameInfo> deviceTypes;
    public boolean obsolete;
    public boolean containsCommunicationActions;

    public DeviceLifeCycleInfo() {
    }

    public DeviceLifeCycleInfo(DeviceLifeCycle deviceLifeCycle) {
        this.id = deviceLifeCycle.getId();
        this.name = deviceLifeCycle.getName();
        this.statesCount = deviceLifeCycle.getFiniteStateMachine().getStates().size();
        this.actionsCount = deviceLifeCycle.getAuthorizedActions().size();
        this.version = deviceLifeCycle.getVersion();
        this.obsolete = deviceLifeCycle.isObsolete();
        this.containsCommunicationActions = deviceLifecycleContainsCommunicationRelatedActions(deviceLifeCycle) ||
                deviceLifecycleContainsCommunicationRelatedChecks(deviceLifeCycle);
    }

    private boolean deviceLifecycleContainsCommunicationRelatedChecks(DeviceLifeCycle deviceLifeCycle) {
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(authorizedAction -> authorizedAction instanceof AuthorizedTransitionAction)
                .flatMap(authorizedAction -> ((AuthorizedTransitionAction) authorizedAction).getChecks()
                        .stream())
                .filter(microCheck -> microCheck.getCategory().equals(MicroCategory.COMMUNICATION))
                .findAny()
                .isPresent();
    }

    private boolean deviceLifecycleContainsCommunicationRelatedActions(DeviceLifeCycle deviceLifeCycle) {
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(authorizedAction -> authorizedAction instanceof AuthorizedTransitionAction)
                .flatMap(authorizedAction1 -> ((AuthorizedTransitionAction) authorizedAction1).getActions().stream())
                .filter(microAction -> microAction.getCategory().equals(MicroCategory.COMMUNICATION))
                .findAny()
                .isPresent();
    }
}
