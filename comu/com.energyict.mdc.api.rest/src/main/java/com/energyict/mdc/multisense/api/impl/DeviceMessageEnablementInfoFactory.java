/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.DeviceMessageEnablement;

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

import static java.util.stream.Collectors.toList;

public class DeviceMessageEnablementInfoFactory extends SelectableFieldFactory<DeviceMessageEnablementInfo, DeviceMessageEnablement> {

    private final Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactoryProvider;

    @Inject
    public DeviceMessageEnablementInfoFactory(Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactory) {
        this.deviceConfigurationInfoFactoryProvider = deviceConfigurationInfoFactory;
    }

    public LinkInfo asLink(DeviceMessageEnablement deviceMessageEnablement, Relation relation, UriInfo uriInfo) {
        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        copySelectedFields(info,deviceMessageEnablement,uriInfo, Arrays.asList("id"));
        info.link = link(deviceMessageEnablement,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageEnablement> deviceMessageEnablements, Relation relation, UriInfo uriInfo) {
        return deviceMessageEnablements.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceMessageEnablement deviceMessageEnablement, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device message enablement")
                .build(deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getId(),
                        deviceMessageEnablement.getDeviceConfiguration().getId(),
                        deviceMessageEnablement.getId());
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
        map.put("id", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> {
            deviceMessageEnablementInfo.id = deviceMessageEnablement.getId();
            if (deviceMessageEnablementInfo.deviceConfiguration == null) {
                deviceMessageEnablementInfo.deviceConfiguration = new DeviceConfigurationInfo();
            }
            deviceMessageEnablementInfo.deviceConfiguration.id = deviceMessageEnablement.getDeviceConfiguration().getId();
            if (deviceMessageEnablementInfo.deviceConfiguration.deviceType == null) {
                deviceMessageEnablementInfo.deviceConfiguration.deviceType = new LinkInfo();
            }
            deviceMessageEnablementInfo.deviceConfiguration.deviceType.id = deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getId();
        });

        map.put("link", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) ->
                deviceMessageEnablementInfo.link = link(deviceMessageEnablement, Relation.REF_SELF, uriInfo)));
        map.put("messageId", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.messageId = deviceMessageEnablement.getDeviceMessageId().dbValue());
        map.put("version", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> {
            if (deviceMessageEnablementInfo.deviceConfiguration == null) {
                deviceMessageEnablementInfo.deviceConfiguration = new DeviceConfigurationInfo();
            }
            deviceMessageEnablementInfo.deviceConfiguration.version = deviceMessageEnablement.getDeviceConfiguration().getVersion();
            if (deviceMessageEnablementInfo.deviceConfiguration.deviceType == null) {
                deviceMessageEnablementInfo.deviceConfiguration.deviceType = new LinkInfo();
            }
            deviceMessageEnablementInfo.deviceConfiguration.deviceType.version = deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getVersion();

        });
        map.put("userActions", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.userActions = deviceMessageEnablement.getUserActions());
        map.put("deviceConfiguration", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) ->
                deviceMessageEnablementInfo.deviceConfiguration = deviceConfigurationInfoFactoryProvider.get().asLink(deviceMessageEnablement.getDeviceConfiguration(), Relation.REF_PARENT, uriInfo)));
        return map;
    }
}
