package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
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

    public LinkInfo asLink(DeviceConfiguration deviceConfiguration, Relation relation, UriInfo uriInfo) {
        return asLink(deviceConfiguration, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<DeviceConfiguration> deviceConfigurations, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return deviceConfigurations.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceConfiguration deviceConfiguration, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = deviceConfiguration.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Device configuration")
                .build(deviceConfiguration.getDeviceType().getId(), deviceConfiguration.getId());
        return info;
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
        map.put("id", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.id = deviceConfiguration.getId();
        });
        map.put("name", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.name = deviceConfiguration.getName();
        });
        map.put("link", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}");
            deviceConfigurationInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_SELF.rel()).title("self reference").build(deviceConfiguration.getDeviceType().getId(), deviceConfiguration.getId());
        });
        map.put("description", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.description = deviceConfiguration.getDescription();
        });
        map.put("deviceType", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.deviceType = new LinkInfo();
            deviceConfigurationInfo.deviceType.id = deviceConfiguration.getDeviceType().getId();
            deviceConfigurationInfo.deviceType.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel(Relation.REF_PARENT.rel()).title("Device type").build(deviceConfiguration.getDeviceType().getId());

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
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(pct.getId());
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
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(enablement.getId());
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
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(sps.getId());
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
                    deviceConfiguration.getDeviceMessageEnablements().stream().map(sps -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = sps.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(sps.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        return map;
    }

}
