/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;


public class DeviceMessageInfoFactory extends SelectableFieldFactory<DeviceMessageInfo, DeviceMessage<?>> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<DeviceInfoFactory> deviceInfoFactoryProvider;
    private final Provider<DeviceMessageSpecificationInfoFactory> deviceMessageSpecificationInfoFactoryProvider;

    @Inject
    public DeviceMessageInfoFactory(MdcPropertyUtils mdcPropertyUtils,
                                    Provider<DeviceInfoFactory> deviceInfoFactory,
                                    Provider<DeviceMessageSpecificationInfoFactory> deviceMessageSpecificationInfoFactory) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceInfoFactoryProvider = deviceInfoFactory;
        this.deviceMessageSpecificationInfoFactoryProvider = deviceMessageSpecificationInfoFactory;
    }

    public LinkInfo asLink(DeviceMessage deviceMessage, Relation relation, UriInfo uriInfo) {
        DeviceMessageInfo info = new DeviceMessageInfo();
        copySelectedFields(info,deviceMessage,uriInfo, Arrays.asList("id","version"));
        info.link = link(deviceMessage,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceMessage> deviceMessages, Relation relation, UriInfo uriInfo) {
        return deviceMessages.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceMessage deviceMessage, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device message")
                .build(((Device)deviceMessage.getDevice()).getmRID(), deviceMessage.getId());
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
        map.put("id", (deviceMessageInfo, deviceMessage, uriInfo) -> {
            deviceMessageInfo.id = deviceMessage.getId();
            if (deviceMessageInfo.device==null) {
                deviceMessageInfo.device = new LinkInfo();
            }
            deviceMessageInfo.device.id = deviceMessage.getDevice().getId();
        });
        map.put("version", (deviceMessageInfo, deviceMessage, uriInfo) -> {
            deviceMessageInfo.version = deviceMessage.getVersion();
            if (deviceMessageInfo.device==null) {
                deviceMessageInfo.device = new LinkInfo();
            }
            deviceMessageInfo.device.version = deviceMessage.getDevice().getId();

        });
        map.put("link", ((deviceMessageInfo, deviceMessage, uriInfo) ->
                deviceMessageInfo.link = link(deviceMessage, Relation.REF_SELF, uriInfo)));
        map.put("device", ((deviceMessageInfo, deviceMessage, uriInfo) ->
                deviceMessageInfo.device = deviceInfoFactoryProvider.get().asLink((Device) deviceMessage.getDevice(), Relation.REF_PARENT, uriInfo)));
        map.put("status", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.status = deviceMessage.getStatus());
        map.put("trackingId", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.trackingId = deviceMessage.getTrackingId());
        map.put("creationDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.creationDate = deviceMessage.getCreationDate());
        map.put("releaseDate", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.releaseDate = deviceMessage.getReleaseDate());
        map.put("user", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.user = deviceMessage.getUser());
        map.put("protocolInfo", (deviceMessageInfo, deviceMessage, uriInfo) -> deviceMessageInfo.protocolInfo = deviceMessage.getProtocolInfo());
        map.put("messageSpecification", (deviceMessageInfo, deviceMessage, uriInfo) ->
                deviceMessageInfo.messageSpecification = deviceMessageSpecificationInfoFactoryProvider.get().asLink(deviceMessage.getSpecification(), Relation.REF_RELATION, uriInfo));
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
