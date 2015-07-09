package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/9/15.
 */
public class DeviceLifecycleActionInfoFactory {
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceLifecycleActionInfoFactory(DeviceLifeCycleService deviceLifeCycleService, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public DeviceLifeCycleActionInfo createDeviceLifecycleActionInfo(Device device, AuthorizedTransitionAction action, UriInfo uriInfo) {
        DeviceLifeCycleActionInfo info = new DeviceLifeCycleActionInfo();
        info.id = action.getId();
        info.name = action.getName();
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                path(DeviceLifecycleActionResource.class).
                path(DeviceLifecycleActionResource.class, "executeAction");
        info.link = Link.fromUriBuilder(uriBuilder).rel("self").build(device.getmRID(), action.getId());
        List<PropertySpec> uniquePropertySpecsForMicroActions =
                DecoratedStream.decorate(action.getActions().stream())
                        .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                        .distinct(PropertySpec::getName)
                        .collect(Collectors.toList());
        Collection<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uniquePropertySpecsForMicroActions, TypedProperties.empty(), device);

        info.properties = new ArrayList<>(propertyInfos);
        info.deviceVersion = device.getVersion();
        return info;
    }



}
