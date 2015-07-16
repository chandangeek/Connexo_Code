package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/9/15.
 */
public class DeviceLifecycleActionInfoFactory extends SelectableFieldFactory<LifeCycleActionInfo, DeviceLifecycleActionInfoFactory.DeviceAction> {
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceLifecycleActionInfoFactory(DeviceLifeCycleService deviceLifeCycleService, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public LifeCycleActionInfo createDeviceLifecycleActionInfo(Device device, AuthorizedTransitionAction action, UriInfo uriInfo, Collection<String> fields) {
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        DeviceAction deviceAction = new DeviceAction();
        deviceAction.action=action;
        deviceAction.device=device;
        copySelectedFields(info, deviceAction, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<LifeCycleActionInfo, DeviceAction>> buildFieldMap() {
        Map<String, PropertyCopier<LifeCycleActionInfo, DeviceAction>> map = new HashMap<>();
        map.put("id", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.id = deviceAction.action.getId());
        map.put("name", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.name = deviceAction.action.getName());
        map.put("link", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(DeviceLifecycleActionResource.class).
                    path(DeviceLifecycleActionResource.class, "executeAction");
            deviceLifeCycleActionInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_SELF).build(deviceAction.device.getmRID(), deviceAction.action.getId());
        });
        map.put("properties", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> {
            List<PropertySpec> uniquePropertySpecsForMicroActions =
                    DecoratedStream.decorate(deviceAction.action.getActions().stream())
                            .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                            .distinct(PropertySpec::getName)
                            .collect(Collectors.toList());

            deviceLifeCycleActionInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uniquePropertySpecsForMicroActions, TypedProperties.empty(), deviceAction.device);
        });
        map.put("deviceVersion", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.deviceVersion = deviceAction.device.getVersion());
        return map;
    }

    class DeviceAction {
        public Device device;
        public AuthorizedTransitionAction action;
    }
}
