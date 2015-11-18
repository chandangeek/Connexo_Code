package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;


public class DeviceMessageInfoFactory extends SelectableFieldFactory<DeviceMessageInfo, DeviceMessage<?>> {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceMessageInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public LinkInfo asLink(DeviceMessage deviceMessage, Relation relation, UriInfo uriInfo) {
        return asLink(deviceMessage, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<DeviceMessage> deviceMessages, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return deviceMessages.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceMessage deviceMessage, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = deviceMessage.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Device message")
                .build(((Device)deviceMessage.getDevice()).getmRID(), deviceMessage.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
                .path(DeviceMessageResource.class, "getDeviceMessage");
    }

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
                    rel(Relation.REF_SELF.rel()).
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
                    rel(Relation.REF_PARENT.rel()).
                    title("Device").
                    build(device.getmRID());
        }
        ));
        map.put("status", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.status = deviceMessage.getStatus());
        map.put("trackingId", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.trackingId = deviceMessage.getTrackingId());
        map.put("creationDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.creationDate = deviceMessage.getCreationDate());
        map.put("releaseDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.releaseDate = deviceMessage.getReleaseDate());
        map.put("user", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.user = deviceMessage.getUser());
        map.put("protocolInfo", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.protocolInfo = deviceMessage.getProtocolInfo());
        map.put("messageSpecification", (deviceMessageInfo, deviceMessage, uriInfo) -> {
            deviceMessageInfo.messageSpecification = new LinkInfo();
            deviceMessageInfo.messageSpecification.id = deviceMessage.getSpecification().getId().dbValue();
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageSpecificationResource.class)
                    .path(DeviceMessageSpecificationResource.class, "getDeviceMessageSpecification")
                    .resolveTemplate("categoryId", deviceMessage.getSpecification().getCategory().getId());

            deviceMessageInfo.messageSpecification.link = Link.fromUriBuilder(uriBuilder).build(deviceMessage.getSpecification().getId().dbValue());


        });
        map.put("deviceMessageAttributes", (deviceMessageInfo, deviceMessage, uriInfo) -> {
            deviceMessageInfo.deviceMessageAttributes = new ArrayList<>();
            TypedProperties typedProperties = TypedProperties.empty();
            deviceMessage.getAttributes().stream().forEach(attribute->typedProperties.setProperty(attribute.getName(), attribute.getValue()));
            List<PropertySpec> propertySpecs = deviceMessage.getAttributes().stream().map(DeviceMessageAttribute::getSpecification).collect(toList());
            mdcPropertyUtils.convertPropertySpecsToPropertyInfos(null, propertySpecs, typedProperties, deviceMessageInfo.deviceMessageAttributes);
        });


        map.put("sentDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.sentDate = deviceMessage.getSentDate().orElse(null));
        return map;
    }
}
