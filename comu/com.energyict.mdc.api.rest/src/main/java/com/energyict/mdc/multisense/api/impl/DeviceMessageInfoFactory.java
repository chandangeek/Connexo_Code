package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class DeviceMessageInfoFactory extends SelectableFieldFactory<DeviceMessageInfo, DeviceMessage<?>> {

    public DeviceMessageInfo from(DeviceMessage deviceMessage, UriInfo uriInfo, Collection<String> fields) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        copySelectedFields(info, deviceMessage, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceMessageInfo, DeviceMessage<?>>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageInfo, DeviceMessage<?>>> map = new HashMap<>();
        map.put("id", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.id = deviceMessage.getId());
        map.put("link", ((deviceMessageInfo, deviceMessage, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.
                    getBaseUriBuilder().
                    path(DeviceMessageResource.class).
                    path(DeviceMessageResource.class, "getDeviceMessage");
            deviceMessageInfo.link = Link.fromUriBuilder(uriBuilder).
                    rel(LinkInfo.REF_SELF).
                    title("Device message").
                    build(((Device)deviceMessage.getDevice()).getmRID(), deviceMessage.getId());
        }
        ));
        map.put("device", ((deviceMessageInfo, deviceMessage, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.
                    getBaseUriBuilder().
                    path(DeviceResource.class).
                    path(DeviceResource.class, "getDevice");
            Device device = (Device) deviceMessage.getDevice();
            deviceMessageInfo.device = new LinkInfo();
            deviceMessageInfo.device.id = device.getId();
            deviceMessageInfo.device.link = Link.fromUriBuilder(uriBuilder).
                    rel(LinkInfo.REF_SELF).
                    title("Device").
                    build(device.getmRID());
        }
        ));

        map.put("status", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.status = deviceMessage.getStatus());
        map.put("sentDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.sentDate = deviceMessage.getSentDate().orElse(null));
        return map;
    }
}
