package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bvn on 7/9/15.
 */
public class DeviceLifecycleActionInfoFactory extends SelectableFieldFactory<LifeCycleActionInfo, DeviceLifecycleActionInfoFactory.DeviceAction> {
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<DeviceInfoFactory> deviceInfoFactoryProvider;

    @Inject
    public DeviceLifecycleActionInfoFactory(DeviceLifeCycleService deviceLifeCycleService,
                                            MdcPropertyUtils mdcPropertyUtils,
                                            Provider<DeviceInfoFactory> deviceInfoFactory) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceInfoFactoryProvider = deviceInfoFactory;
    }

    public LinkInfo asLink(Device device, AuthorizedTransitionAction action, Relation relation, UriInfo uriInfo) {
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        DeviceAction deviceAction = new DeviceAction();
        deviceAction.action = action;
        deviceAction.device = device;
        copySelectedFields(info, deviceAction, uriInfo, Arrays.asList("id", "version"));
        info.link = link(device,action,relation,uriInfo);
        return info;
    }

    private Link link(Device device, AuthorizedTransitionAction action, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device lifecycle action")
                .build(device.getmRID(), action.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().
                path(DeviceLifecycleActionResource.class).
                path(DeviceLifecycleActionResource.class, "getAction");
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
        map.put("version", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.version = deviceAction.action.getVersion());
        map.put("name", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.name = deviceAction.action.getName());
        map.put("link", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.link = link(deviceAction.device, deviceAction.action, Relation.REF_SELF, uriInfo));
        map.put("properties", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> {
            List<PropertySpec> uniquePropertySpecsForMicroActions =
                    DecoratedStream.decorate(deviceAction.action.getActions().stream())
                            .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                            .distinct(PropertySpec::getName)
                            .collect(Collectors.toList());

            deviceLifeCycleActionInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uniquePropertySpecsForMicroActions, TypedProperties.empty(), deviceAction.device);
        });
        map.put("device", (deviceLifeCycleActionInfo, deviceAction, uriInfo) -> deviceLifeCycleActionInfo.device = deviceInfoFactoryProvider.get().asLink(deviceAction.device, Relation.REF_PARENT, uriInfo));
        return map;
    }

    class DeviceAction {
        public Device device;
        public AuthorizedTransitionAction action;
    }
}
