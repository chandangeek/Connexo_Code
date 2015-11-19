package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DeviceMessageEnablementInfoFactory extends SelectableFieldFactory<DeviceMessageEnablementInfo, DeviceMessageEnablement> {

    private final Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactoryProvider;

    @Inject
    public DeviceMessageEnablementInfoFactory(Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactory) {
        this.deviceConfigurationInfoFactoryProvider = deviceConfigurationInfoFactory;
    }

    public LinkInfo asLink(DeviceMessageEnablement deviceMessageEnablement, Relation relation, UriInfo uriInfo) {
        return asLink(deviceMessageEnablement, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageEnablement> deviceMessageEnablements, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return deviceMessageEnablements.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceMessageEnablement deviceMessageEnablement, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = deviceMessageEnablement.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Device message enablement")
                .build(deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getId(),
                        deviceMessageEnablement.getDeviceConfiguration().getId(),
                        deviceMessageEnablement.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageEnablementResource.class)
                    .path(DeviceMessageEnablementResource.class, "getDeviceMessageEnablement");
    }


    public DeviceMessageEnablementInfo from(DeviceMessageEnablement deviceMessageEnablement, UriInfo uriInfo, Collection<String> fields) {
        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        copySelectedFields(info, deviceMessageEnablement, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceMessageEnablementInfo, DeviceMessageEnablement>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageEnablementInfo, DeviceMessageEnablement>> map = new HashMap<>();
        map.put("id", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.id = deviceMessageEnablement.getId());
        map.put("link", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) ->
                deviceMessageEnablementInfo.link = asLink(deviceMessageEnablement, Relation.REF_SELF, uriInfo).link));
        map.put("messageId", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.messageId = deviceMessageEnablement.getDeviceMessageId().dbValue());
        map.put("userActions", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.userActions = deviceMessageEnablement.getUserActions());
        map.put("deviceConfiguration", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) ->
                deviceMessageEnablementInfo.deviceConfiguration = deviceConfigurationInfoFactoryProvider.get().asLink(deviceMessageEnablement.getDeviceConfiguration(), Relation.REF_PARENT, uriInfo)));
        return map;
    }
}
