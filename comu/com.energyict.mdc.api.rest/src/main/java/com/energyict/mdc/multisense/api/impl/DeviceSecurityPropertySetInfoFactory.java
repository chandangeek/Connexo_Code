/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class DeviceSecurityPropertySetInfoFactory extends SelectableFieldFactory<DeviceSecurityPropertySetInfo, Pair<Device, SecurityPropertySet>> {

    private final ConfigurationSecurityPropertySetInfoFactory configurationSecurityPropertySetInfoFactory;
    private final DeviceInfoFactory deviceInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<KeyAccessorInfoFactory> keyAccessorInfoFactoryProvider;

    @Inject
    public DeviceSecurityPropertySetInfoFactory(ConfigurationSecurityPropertySetInfoFactory configurationSecurityPropertySetInfoFactory, DeviceInfoFactory deviceInfoFactory, MdcPropertyUtils mdcPropertyUtils, Provider<KeyAccessorInfoFactory> keyAccessorInfoFactoryProvider) {
        this.configurationSecurityPropertySetInfoFactory = configurationSecurityPropertySetInfoFactory;
        this.deviceInfoFactory = deviceInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.keyAccessorInfoFactoryProvider = keyAccessorInfoFactoryProvider;
    }

    public LinkInfo asLink(Pair<Device, SecurityPropertySet> source, Relation relation, UriInfo uriInfo) {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        copySelectedFields(info, source, uriInfo, Arrays.asList("id", "version"));
        info.link = link(source.getFirst(), source.getLast(), relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Pair<Device, SecurityPropertySet>> sources, Relation relation, UriInfo uriInfo) {
        return sources.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(Device device, SecurityPropertySet deviceSecurityPropertySet, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Security property set values")
                .build(device.getmRID(), deviceSecurityPropertySet.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceSecurityPropertySetResource.class)
                .path(DeviceSecurityPropertySetResource.class, "getDeviceSecurityPropertySet");
    }

    public DeviceSecurityPropertySetInfo from(Device device, SecurityPropertySet deviceSecurityPropertySet, UriInfo uriInfo, Collection<String> fields) {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        copySelectedFields(info, Pair.of(device, deviceSecurityPropertySet), uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceSecurityPropertySetInfo, Pair<Device, SecurityPropertySet>>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceSecurityPropertySetInfo, Pair<Device, SecurityPropertySet>>> map = new HashMap<>();
        map.put("id", (deviceSecurityPropertySetInfo, source, uriInfo) -> {
            deviceSecurityPropertySetInfo.id = source.getLast().getId();
            if (deviceSecurityPropertySetInfo.device == null) {
                deviceSecurityPropertySetInfo.device = new LinkInfo();
            }
            deviceSecurityPropertySetInfo.device.id = source.getFirst().getId();
        });
        map.put("version", (deviceSecurityPropertySetInfo, source, uriInfo) -> {
            deviceSecurityPropertySetInfo.version = source.getLast().getVersion();
            if (deviceSecurityPropertySetInfo.device == null) {
                deviceSecurityPropertySetInfo.device = new LinkInfo();
            }
            deviceSecurityPropertySetInfo.device.version = source.getFirst().getVersion();

        });
        map.put("link", ((deviceSecurityPropertySetInfo, source, uriInfo) ->
                deviceSecurityPropertySetInfo.link = link(source.getFirst(), source.getLast(), Relation.REF_SELF, uriInfo)));
        map.put("device", ((deviceSecurityPropertySetInfo, source, uriInfo) ->
                deviceSecurityPropertySetInfo.device = deviceInfoFactory.asLink(source.getFirst(), Relation.REF_PARENT, uriInfo)));
        map.put("configuredSecurityPropertySet", ((deviceSecurityPropertySetInfo, source, uriInfo) ->
                deviceSecurityPropertySetInfo.configuredSecurityPropertySet = configurationSecurityPropertySetInfoFactory.asLink(source.getLast(), Relation.REF_RELATION, uriInfo)));
        map.put("properties", (deviceSecurityPropertySetInfo, source, uriInfo) -> {
            List<SecurityAccessor> securityAccessors = findInvolvedKeyAccessors(source.getFirst(), source.getLast());
            deviceSecurityPropertySetInfo.properties = new ArrayList<>(securityAccessors.size());
            for (SecurityAccessor securityAccessor : securityAccessors) {
                deviceSecurityPropertySetInfo.properties.add(
                        keyAccessorInfoFactoryProvider.get().asLink(
                                source.getFirst(),
                                securityAccessor,
                                Relation.REF_RELATION,
                                uriInfo
                        )
                );
            }
        });
        return map;
    }

    private List<SecurityAccessor> findInvolvedKeyAccessors(Device device, SecurityPropertySet securityPropertySet) {
        return device.getSecurityAccessors().stream()
                .filter(keyAccessor ->
                        securityPropertySet.getConfigurationSecurityProperties().stream().anyMatch(configurationSecurityProperty ->
                                configurationSecurityProperty.getSecurityAccessorType().getId() == keyAccessor.getSecurityAccessorType().getId()
                        )
                )
                .collect(Collectors.toList());
    }

    private PropertyInfo getClientAsPropertyInfo(SecurityPropertySet securityPropertySet) {
        if (securityPropertySet.getClientSecurityPropertySpec().isPresent()) {
            return mdcPropertyUtils.convertPropertySpecToPropertyInfo(securityPropertySet.getClientSecurityPropertySpec().get(), securityPropertySet.getClient());
        } else {
            return null;
        }
    }
}
