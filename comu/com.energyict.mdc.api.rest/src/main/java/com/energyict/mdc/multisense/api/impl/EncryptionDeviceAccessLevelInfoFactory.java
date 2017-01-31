/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class EncryptionDeviceAccessLevelInfoFactory extends SelectableFieldFactory<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>> {

    private final MdcPropertyUtils mdcPropertyUtils;

    public LinkInfo asLink(DeviceProtocolPluggableClass protocolPluggableClass, DeviceAccessLevel deviceAccessLevel, Relation relation, UriInfo uriInfo) {
        DeviceAccessLevelInfo info = new DeviceAccessLevelInfo();
        copySelectedFields(info, Pair.of(protocolPluggableClass, deviceAccessLevel), uriInfo, Arrays.asList("id"));
        info.link = link(protocolPluggableClass, deviceAccessLevel, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>> pairs, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return pairs.stream().map(i-> asLink(i.getFirst(), i.getLast(), relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceProtocolPluggableClass protocolPluggableClass, DeviceAccessLevel deviceAccessLevel, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Encryption access level").
                build(protocolPluggableClass.getId(),deviceAccessLevel.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(EncryptionDeviceAccessLevelResource.class)
                .path(EncryptionDeviceAccessLevelResource.class, "getEncryptionDeviceAccessLevel");
    }


    @Inject
    public EncryptionDeviceAccessLevelInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public DeviceAccessLevelInfo from(DeviceProtocolPluggableClass pluggableClass, DeviceAccessLevel authenticationDeviceAccessLevel, UriInfo uriInfo, Collection<String> fields) {
        DeviceAccessLevelInfo info = new DeviceAccessLevelInfo();
        copySelectedFields(info, Pair.of(pluggableClass,authenticationDeviceAccessLevel), uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>>> map = new HashMap<>();
        map.put("id", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.id = (long) pair.getLast().getId());
        map.put("name", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.name = pair.getLast().getTranslation());
        map.put("properties", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(pair.getLast().getSecurityProperties(), TypedProperties.empty()));
        map.put("link", ((deviceAccessLevelInfo, deviceAccessLevel, uriInfo) ->
            deviceAccessLevelInfo.link = link(deviceAccessLevel.getFirst(), deviceAccessLevel.getLast(), Relation.REF_SELF, uriInfo)));

        return map;
    }
}
