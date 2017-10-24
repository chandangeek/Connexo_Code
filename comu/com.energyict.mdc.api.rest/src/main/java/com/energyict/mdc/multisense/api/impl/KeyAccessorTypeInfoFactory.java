package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

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
 * SelectableFieldFactory<KeyAccessorTypeInfo, KeyAccessorType><br/>
 * Note that methods are provided to create a link based on:
 * <ul>
 *     <li>a given device and KeyAccessorType (~ corresponding to KeyAccessorTypeResource)</li>
 *     <li>a given deviceType and KeyAccessorType (~corresponding to ConfigurationKeyAccessorTypeResource)</li>
 * </ul>
 *
 */
public class KeyAccessorTypeInfoFactory extends SelectableFieldFactory<KeyAccessorTypeInfo, SecurityAccessorType> {

    public LinkInfo asLink(Device device, SecurityAccessorType securityAccessorType, Relation relation, UriInfo uriInfo) {
        KeyAccessorTypeInfo info = new KeyAccessorTypeInfo();
        copySelectedFields(info, securityAccessorType, uriInfo, Collections.singletonList("id"));
        info.link = link(device, securityAccessorType, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Pair<Device, SecurityAccessorType>> keyAccessortypes, Relation relation, UriInfo uriInfo) {
        return keyAccessortypes.stream().map(i -> asLink(i.getFirst(), i.getLast(), relation, uriInfo)).collect(toList());
    }

    private Link link(Device device, SecurityAccessorType securityAccessorType, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getDeviceUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Key accessor type")
                .build(device.getmRID(), securityAccessorType.getName());
    }

    private UriBuilder getDeviceUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(KeyAccessorTypeResource.class)
                .path(KeyAccessorTypeResource.class, "getKeyAccessorType");
    }

    public LinkInfo asLink(DeviceType deviceType, SecurityAccessorType securityAccessorType, Relation relation, UriInfo uriInfo) {
        KeyAccessorTypeInfo info = new KeyAccessorTypeInfo();
        copySelectedFields(info, securityAccessorType, uriInfo, Collections.singletonList("id"));
        info.link = link(deviceType, securityAccessorType, relation, uriInfo);
        return info;
    }

    private Link link(DeviceType deviceType, SecurityAccessorType securityAccessorType, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getDeviceTypeUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Key accessor type")
                .build(deviceType.getId(), securityAccessorType.getName());
    }

    private UriBuilder getDeviceTypeUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ConfigurationKeyAccessorTypeResource.class)
                .path(ConfigurationKeyAccessorTypeResource.class, "getKeyAccessorType");
    }

    public KeyAccessorTypeInfo from(SecurityAccessorType securityAccessorType, UriInfo uriInfo, Collection<String> fields) {
        KeyAccessorTypeInfo info = new KeyAccessorTypeInfo();
        copySelectedFields(info, securityAccessorType, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<KeyAccessorTypeInfo, SecurityAccessorType>> buildFieldMap() {
        Map<String, PropertyCopier<KeyAccessorTypeInfo, SecurityAccessorType>> map = new HashMap<>();
        map.put("id", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.id = keyAccessorType.getId()));
        map.put("name", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.name = keyAccessorType.getName()));
        map.put("description", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.description = keyAccessorType.getDescription()));
        return map;
    }
}