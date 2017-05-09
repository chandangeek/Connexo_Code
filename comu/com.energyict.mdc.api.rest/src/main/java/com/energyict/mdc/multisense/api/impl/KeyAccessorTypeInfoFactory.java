package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class KeyAccessorTypeInfoFactory extends SelectableFieldFactory<KeyAccessorTypeInfo, KeyAccessorType> {

    public LinkInfo asLink(KeyAccessorType keyAccessorType, Relation relation, UriInfo uriInfo) {
        KeyAccessorTypeInfo info = new KeyAccessorTypeInfo();
        copySelectedFields(info, keyAccessorType, uriInfo, Arrays.asList("id"));
        info.link = link(keyAccessorType, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<KeyAccessorType> keyAccessortypes, Relation relation, UriInfo uriInfo) {
        return keyAccessortypes.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(KeyAccessorType keyAccessorType, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Key accessor type")
                .build(keyAccessorType.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(KeyAccessorTypeResource.class)
                .path(KeyAccessorTypeResource.class, "getKeyAccessorType");
    }

    public KeyAccessorTypeInfo from(KeyAccessorType keyAccessorType, UriInfo uriInfo, Collection<String> fields) {
        KeyAccessorTypeInfo info = new KeyAccessorTypeInfo();
        copySelectedFields(info, keyAccessorType, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<KeyAccessorTypeInfo, KeyAccessorType>> buildFieldMap() {
        Map<String, PropertyCopier<KeyAccessorTypeInfo, KeyAccessorType>> map = new HashMap<>();
        map.put("id", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.id = keyAccessorType.getId()));
        map.put("name", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.name = keyAccessorType.getName()));
        map.put("description", ((keyAccessorTypeInfo, keyAccessorType, uriInfo) -> keyAccessorTypeInfo.description = keyAccessorType.getDescription()));
        return map;
    }
}