/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * SelectableFieldFactory<KeyAccessorInfo, KeyAccessor>
 */
public class KeyAccessorInfoFactory extends SelectableFieldFactory<KeyAccessorInfo, SecurityAccessor> {

    private final Provider<KeyAccessorTypeInfoFactory> keyAccessorTypeInfoFactoryProvider;

    @Inject
    public KeyAccessorInfoFactory(Provider<KeyAccessorTypeInfoFactory> keyAccessorTypeInfoFactoryProvider) {
        this.keyAccessorTypeInfoFactoryProvider = keyAccessorTypeInfoFactoryProvider;
    }

    public LinkInfo asLink(Device device, SecurityAccessor securityAccessor, Relation relation, UriInfo uriInfo) {
        KeyAccessorInfo info = new KeyAccessorInfo();
        copySelectedFields(info, securityAccessor, uriInfo, Collections.singletonList("id"));
        info.link = link(device, securityAccessor, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Pair<Device, SecurityAccessor>> keyAccessors, Relation relation, UriInfo uriInfo) {
        return keyAccessors.stream().map(i -> asLink(i.getFirst(), i.getLast(), relation, uriInfo)).collect(toList());
    }

    private Link link(Device device, SecurityAccessor securityAccessor, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getDeviceUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Key accessor")
                .build(device.getmRID(), securityAccessor.getSecurityAccessorType().getName());
    }

    private UriBuilder getDeviceUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(KeyAccessorResource.class)
                .path(KeyAccessorResource.class, "getKeyAccessor");
    }

    public KeyAccessorInfo from(SecurityAccessor securityAccessor, UriInfo uriInfo, Collection<String> fields) {
        KeyAccessorInfo info = new KeyAccessorInfo();
        copySelectedFields(info, securityAccessor, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<KeyAccessorInfo, SecurityAccessor>> buildFieldMap() {
        Map<String, PropertyCopier<KeyAccessorInfo, SecurityAccessor>> map = new HashMap<>();
        map.put("name", ((keyAccessorInfo, keyAccessor, uriInfo) -> keyAccessorInfo.name = keyAccessor.getSecurityAccessorType().getName()));
        map.put("keyAccessorType", ((keyAccessorInfo, keyAccessor, uriInfo) -> keyAccessorInfo.keyAccessorType = keyAccessorTypeInfoFactoryProvider.get().asLink(
                keyAccessor.getDevice().getDeviceType(),
                keyAccessor.getSecurityAccessorType(),
                Relation.REF_RELATION,
                uriInfo
        )));
        return map;
    }
}
