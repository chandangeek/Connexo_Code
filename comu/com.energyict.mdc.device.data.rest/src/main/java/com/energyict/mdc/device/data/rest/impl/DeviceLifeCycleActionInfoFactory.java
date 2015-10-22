package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionInfoFactory {

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceLifeCycleActionInfoFactory(DeviceLifeCycleService deviceLifeCycleService, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public DeviceLifeCycleActionInfo from(ExecutableAction executableAction){
        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.device = DeviceInfo.from(executableAction.getDevice());
        if (executableAction != null && executableAction.getAction() instanceof AuthorizedTransitionAction){
            info.id = executableAction.getAction().getId();
            info.name = executableAction.getAction().getName();
            AuthorizedTransitionAction castedAction = (AuthorizedTransitionAction) executableAction.getAction();
            List<PropertySpec> uniquePropertySpecsForMicroActions =
                    DecoratedStream.decorate(castedAction.getActions().stream())
                            .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                            .distinct(PropertySpec::getName)
                            .collect(Collectors.toList());
            Collection<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uniquePropertySpecsForMicroActions, TypedProperties.empty(), executableAction.getDevice());
            info.properties = new ArrayList<>(propertyInfos);
        }
        // For now there is no available actions for BPM transition
        return info;
    }
}
