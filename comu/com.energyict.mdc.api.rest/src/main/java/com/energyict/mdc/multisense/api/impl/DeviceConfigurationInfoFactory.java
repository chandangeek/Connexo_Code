/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;

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
public class DeviceConfigurationInfoFactory extends SelectableFieldFactory<DeviceConfigurationInfo, DeviceConfiguration> {

    private final Provider<DeviceTypeInfoFactory> deviceTypeInfoFactoryProvider;
    private final Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider;
    private final Provider<ComTaskEnablementInfoFactory> comTaskEnablementInfoFactoryProvider;
    private final Provider<ConfigurationSecurityPropertySetInfoFactory> configurationSecurityPropertySetInfoFactoryProvider;
    private final Provider<DeviceMessageEnablementInfoFactory> deviceMessageEnablementInfoFactory;

    @Inject
    public DeviceConfigurationInfoFactory(Provider<DeviceTypeInfoFactory> deviceTypeInfoFactoryProvider,
                                          Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider,
                                          Provider<ComTaskEnablementInfoFactory> comTaskEnablementInfoFactoryProvider,
                                          Provider<ConfigurationSecurityPropertySetInfoFactory> configurationSecurityPropertySetInfoFactoryProvider,
                                          Provider<DeviceMessageEnablementInfoFactory> deviceMessageEnablementInfoFactory) {
        this.deviceTypeInfoFactoryProvider = deviceTypeInfoFactoryProvider;
        this.partialConnectionTaskInfoFactoryProvider = partialConnectionTaskInfoFactoryProvider;
        this.comTaskEnablementInfoFactoryProvider = comTaskEnablementInfoFactoryProvider;
        this.configurationSecurityPropertySetInfoFactoryProvider = configurationSecurityPropertySetInfoFactoryProvider;
        this.deviceMessageEnablementInfoFactory = deviceMessageEnablementInfoFactory;
    }

    public DeviceConfigurationInfo asLink(DeviceConfiguration deviceConfiguration, Relation relation, UriInfo uriInfo) {
        DeviceConfigurationInfo info = new DeviceConfigurationInfo();
        copySelectedFields(info,deviceConfiguration,uriInfo, Arrays.asList("id","version"));
        info.link = link(deviceConfiguration,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceConfiguration> deviceConfigurations, Relation relation, UriInfo uriInfo) {
        return deviceConfigurations.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceConfiguration deviceConfiguration, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device configuration")
                .build(deviceConfiguration.getDeviceType().getId(), deviceConfiguration.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceConfigurationResource.class)
                .path(DeviceConfigurationResource.class, "getDeviceConfiguration");
    }

    public DeviceConfigurationInfo from(DeviceConfiguration deviceConfiguration, UriInfo uriInfo, List<String> fields) {
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        copySelectedFields(deviceConfigurationInfo, deviceConfiguration, uriInfo, fields);
        return deviceConfigurationInfo;
    }

    protected Map<String, PropertyCopier<DeviceConfigurationInfo,DeviceConfiguration>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceConfigurationInfo, DeviceConfiguration>> map = new HashMap<>();
        map.put("id", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.id = deviceConfiguration.getId());
        map.put("version", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.version = deviceConfiguration.getVersion());
        map.put("name", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.name = deviceConfiguration.getName());
        map.put("link", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.link = link(deviceConfiguration, Relation.REF_SELF, uriInfo));
        map.put("description", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.description = deviceConfiguration.getDescription());
        map.put("deviceType", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.deviceType = deviceTypeInfoFactoryProvider.get().asLink(deviceConfiguration.getDeviceType(), Relation.REF_PARENT, uriInfo));
        map.put("connectionMethods", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.connectionMethods = partialConnectionTaskInfoFactoryProvider.get().asLink(deviceConfiguration.getPartialConnectionTasks(), Relation.REF_RELATION, uriInfo));
        map.put("comTaskEnablements", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.comTaskEnablements = comTaskEnablementInfoFactoryProvider.get().asLink(deviceConfiguration.getComTaskEnablements(), Relation.REF_RELATION, uriInfo));
        map.put("securityPropertySets", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.securityPropertySets = configurationSecurityPropertySetInfoFactoryProvider.get().asLink(deviceConfiguration.getSecurityPropertySets(), Relation.REF_RELATION, uriInfo));
        map.put("deviceMessageEnablements", (deviceConfigurationInfo, deviceConfiguration, uriInfo) ->
            deviceConfigurationInfo.deviceMessageEnablements = deviceMessageEnablementInfoFactory.get().asLink(deviceConfiguration.getDeviceMessageEnablements(), Relation.REF_RELATION, uriInfo));
        return map;
    }

}
