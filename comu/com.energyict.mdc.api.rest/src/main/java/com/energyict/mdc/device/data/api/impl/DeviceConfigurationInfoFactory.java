package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceConfigurationInfoFactory {

    @Inject
    public DeviceConfigurationInfoFactory() {
    }

    public DeviceConfigurationInfo plain(DeviceConfiguration deviceConfiguration, Collection<String> fields) {
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(deviceConfigurationInfo, deviceConfiguration, Optional.empty()));
        return deviceConfigurationInfo;
    }

    public DeviceConfigurationInfo asHypermedia(DeviceConfiguration deviceConfiguration, UriInfo uriInfo, List<String> fields) {
        DeviceConfigurationInfo DeviceConfigurationInfo = new DeviceConfigurationInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(DeviceConfigurationInfo, deviceConfiguration, Optional.of(uriInfo)));
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}");
        DeviceConfigurationInfo.link = Link.fromUriBuilder(uriBuilder).rel("self").title("self reference").build(deviceConfiguration.getDeviceType().getId(), deviceConfiguration.getId());
        return DeviceConfigurationInfo;
    }

    private List<PropertyCopier<DeviceConfigurationInfo, DeviceConfiguration>> getSelectedFields(Collection<String> fields) {
        Map<String, PropertyCopier<DeviceConfigurationInfo, DeviceConfiguration>> fieldSelectionMap = buildFieldSelectionMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldSelectionMap.keySet();
        }
        return fields.stream().filter(fieldSelectionMap::containsKey).map(fieldSelectionMap::get).collect(toList());
    }

    private Map<String, PropertyCopier<DeviceConfigurationInfo,DeviceConfiguration>> buildFieldSelectionMap() {
        Map<String, PropertyCopier<DeviceConfigurationInfo, DeviceConfiguration>> map = new HashMap<>();
        map.put("id", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.id = deviceConfiguration.getId();
        });
        map.put("name", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.name = deviceConfiguration.getName();
        });
        map.put("description", (deviceConfigurationInfo, deviceConfiguration, uriInfo) -> {
            deviceConfigurationInfo.description = deviceConfiguration.getDescription();
        });
        return map;
    }

}
