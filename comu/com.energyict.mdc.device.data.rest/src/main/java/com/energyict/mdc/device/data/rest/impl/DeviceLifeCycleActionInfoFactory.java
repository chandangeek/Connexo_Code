package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
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
        if (executableAction != null && executableAction.getAction() instanceof AuthorizedTransitionAction){
            info.id = executableAction.getAction().getId();
            info.name = executableAction.getAction().getName();
            AuthorizedTransitionAction castedAction = (AuthorizedTransitionAction) executableAction.getAction();
            List<PropertySpec> allPropertySpecsForMicroActions = castedAction.getActions()
                    .stream()
                    .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                    .collect(Collectors.toList());
            // Remove duplicates with the same key
            Collection<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(allPropertySpecsForMicroActions, TypedProperties.empty(), executableAction.getDevice())
                    .stream()
                    .collect(Collectors.toMap(propertyInfo -> propertyInfo.key, Function.<PropertyInfo>identity(), (prop1, prop2) -> prop1))
                    .values();
            info.properties = new ArrayList<>(propertyInfos);
        }
        // For now there is no available actions for BPM transition
        return info;
    }
}
