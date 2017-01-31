/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.DeviceType;

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

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceTypeInfoFactory extends SelectableFieldFactory<DeviceTypeInfo, DeviceType> {

    private final Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactoryProvider;
    private final Provider<DeviceMessageFileInfoFactory> deviceMessageFileInfoFactoryProvider;

    @Inject
    public DeviceTypeInfoFactory(Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactory, Provider<DeviceMessageFileInfoFactory> deviceMessageFileInfoFactoryProvider) {
        this.deviceConfigurationInfoFactoryProvider = deviceConfigurationInfoFactory;
        this.deviceMessageFileInfoFactoryProvider = deviceMessageFileInfoFactoryProvider;
    }

    public LinkInfo asLink(DeviceType deviceType, Relation relation, UriInfo uriInfo) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        copySelectedFields(deviceTypeInfo, deviceType, uriInfo, Arrays.asList("id", "version"));
        deviceTypeInfo.link = link(deviceType, relation, uriInfo);
        return deviceTypeInfo;
    }

    public List<LinkInfo> asLink(Collection<DeviceType> deviceTypes, Relation relation, UriInfo uriInfo) {
        return deviceTypes.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceType deviceType, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device type")
                .build(deviceType.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceTypeResource.class)
                .path(DeviceTypeResource.class, "getDeviceType");
    }

    public DeviceTypeInfo from(DeviceType deviceType, UriInfo uriInfo, List<String> fields) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        copySelectedFields(deviceTypeInfo, deviceType, uriInfo, fields);
        return deviceTypeInfo;
    }

    protected Map<String, PropertyCopier<DeviceTypeInfo,DeviceType>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceTypeInfo, DeviceType>> map = new HashMap<>();
        map.put("id", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.id = deviceType.getId();
        });
        map.put("version", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.version = deviceType.getVersion();
        });
        map.put("name", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.name = deviceType.getName();
        });
        map.put("link", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.link = link(deviceType, Relation.REF_SELF, uriInfo);
        });
        map.put("description", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.description = deviceType.getDescription();
        });
        map.put("deviceConfigurations", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.deviceConfigurations = deviceConfigurationInfoFactoryProvider.get().asLink(deviceType.getConfigurations(), Relation.REF_RELATION, uriInfo);
        });
        map.put("deviceMessageFiles", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.deviceMessageFiles = deviceMessageFileInfoFactoryProvider.get().asLink(deviceType.getDeviceMessageFiles(), Relation.REF_RELATION, uriInfo);
        });
        return map;
    }

}
