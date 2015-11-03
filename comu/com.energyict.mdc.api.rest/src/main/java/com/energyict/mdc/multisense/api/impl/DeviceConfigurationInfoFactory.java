package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceConfigurationInfoFactory extends SelectableFieldFactory<DeviceConfigurationInfo, DeviceConfiguration> {

    @Inject
    public DeviceConfigurationInfoFactory() {
    }

    public DeviceConfigurationInfo from(DeviceConfiguration deviceConfiguration, UriInfo uriInfo, List<String> fields) {
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        copySelectedFields(deviceConfigurationInfo, deviceConfiguration, uriInfo, fields);
        return deviceConfigurationInfo;
    }

    protected Map<String, PropertyCopier<DeviceConfigurationInfo,DeviceConfiguration>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceConfigurationInfo, DeviceConfiguration>> map = new HashMap<>();
        map.put("id", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.id = deviceConfiguration.getId();
        });
        map.put("name", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.name = deviceConfiguration.getName();
        });
        map.put("link", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}");
            deviceConfigurationInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_SELF).title("self reference").build(deviceConfiguration.getDeviceType().getId(), deviceConfiguration.getId());
        });
        map.put("description", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.description = deviceConfiguration.getDescription();
        });
        map.put("deviceType", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.deviceType = new LinkInfo();
            deviceConfigurationInfo.deviceType.id = deviceConfiguration.getDeviceType().getId();
            deviceConfigurationInfo.deviceType.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel(LinkInfo.REF_PARENT).title("Device type").build(deviceConfiguration.getDeviceType().getId());

        });
        map.put("connectionMethods", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(PartialConnectionTaskResource.class)
                    .path(PartialConnectionTaskResource.class, "getPartialConnectionTask")
                    .resolveTemplate("deviceTypeId", deviceConfiguration.getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", deviceConfiguration.getId());
            deviceConfigurationInfo.connectionMethods =
                    deviceConfiguration.getPartialConnectionTasks().stream().map(pct -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = pct.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(pct.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("comTaskEnablements", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(ComTaskEnablementResource.class)
                    .path(ComTaskEnablementResource.class, "getComTaskEnablement")
                    .resolveTemplate("deviceTypeId", deviceConfiguration.getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", deviceConfiguration.getId());
            deviceConfigurationInfo.comTaskEnablements =
                    deviceConfiguration.getComTaskEnablements().stream().map(enablement -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = enablement.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(enablement.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("securityPropertySets", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(ConfigurationSecurityPropertySetResource.class)
                    .path(ConfigurationSecurityPropertySetResource.class, "getSecurityPropertySet")
                    .resolveTemplate("deviceTypeId", deviceConfiguration.getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", deviceConfiguration.getId());
            deviceConfigurationInfo.securityPropertySets =
                    deviceConfiguration.getSecurityPropertySets().stream().map(sps -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = sps.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(sps.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("deviceMessageEnablements", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageEnablementResource.class)
                    .path(DeviceMessageEnablementResource.class, "getDeviceMessageEnablement")
                    .resolveTemplate("deviceTypeId", deviceConfiguration.getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", deviceConfiguration.getId());
            deviceConfigurationInfo.deviceMessageEnablements =
                    deviceConfiguration.getSecurityPropertySets().stream().map(sps -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = sps.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(sps.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        return map;
    }

}
