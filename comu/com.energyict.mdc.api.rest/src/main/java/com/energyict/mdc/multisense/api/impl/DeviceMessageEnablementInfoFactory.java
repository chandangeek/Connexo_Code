package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeviceMessageEnablementInfoFactory extends SelectableFieldFactory<DeviceMessageEnablementInfo, DeviceMessageEnablement> {

    public DeviceMessageEnablementInfo from(DeviceMessageEnablement deviceMessageEnablement, UriInfo uriInfo, Collection<String> fields) {
        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        copySelectedFields(info, deviceMessageEnablement, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceMessageEnablementInfo, DeviceMessageEnablement>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageEnablementInfo, DeviceMessageEnablement>> map = new HashMap<>();
        map.put("id", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.id = deviceMessageEnablement.getId());
        map.put("link", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(DeviceMessageEnablementResource.class)
                    .path(DeviceMessageEnablementResource.class, "getDeviceMessageEnablement")
                    .resolveTemplate("deviceTypeId", deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", deviceMessageEnablement.getDeviceConfiguration().getId());

            deviceMessageEnablementInfo.link = Link.fromUriBuilder(uriBuilder).
                    rel(LinkInfo.REF_SELF).
                    title("Device configuration").
                    build(deviceMessageEnablement.getId());
        }
        ));

        map.put("messageId", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.messageId = deviceMessageEnablement.getDeviceMessageId().dbValue());
        map.put("userActions", (deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> deviceMessageEnablementInfo.userActions = deviceMessageEnablement.getUserActions());
        map.put("deviceConfiguration", ((deviceMessageEnablementInfo, deviceMessageEnablement, uriInfo) -> {
            UriBuilder uirBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(DeviceConfigurationResource.class)
                    .path(DeviceConfigurationResource.class, "getDeviceConfiguration")
                    .resolveTemplate("deviceTypeId", deviceMessageEnablement.getDeviceConfiguration().getDeviceType().getId());
            deviceMessageEnablementInfo.deviceConfiguration = new LinkInfo();
            deviceMessageEnablementInfo.deviceConfiguration.id = deviceMessageEnablement.getDeviceConfiguration().getId();
            deviceMessageEnablementInfo.deviceConfiguration.link = Link.fromUriBuilder(uirBuilder)
                    .rel(LinkInfo.REF_PARENT)
                    .title("Device configuration")
                    .build(deviceMessageEnablement.getDeviceConfiguration().getId());
        }));

        return map;
    }
}
