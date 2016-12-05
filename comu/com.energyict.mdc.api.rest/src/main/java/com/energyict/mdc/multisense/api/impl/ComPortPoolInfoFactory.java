package com.energyict.mdc.multisense.api.impl;


import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.engine.config.ComPortPool;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/14/15.
 */
public class ComPortPoolInfoFactory extends SelectableFieldFactory<ComPortPoolInfo, ComPortPool> {

    public LinkInfo asLink(ComPortPool comPortPool, Relation relation, UriInfo uriInfo) {
        ComPortPoolInfo info = new ComPortPoolInfo();
        copySelectedFields(info, comPortPool, uriInfo, Arrays.asList("id", "version"));
        info.link=link(comPortPool,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComPortPool> comPortPools, Relation relation, UriInfo uriInfo) {
        return comPortPools.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ComPortPool comPortPool, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Communication port pool")
                .build(comPortPool.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ComPortPoolResource.class)
                .path(ComPortPoolResource.class, "getComPortPool");
    }

    public ComPortPoolInfo from(ComPortPool comPortPool, UriInfo uriInfo, Collection<String> fields) {
        ComPortPoolInfo info = new ComPortPoolInfo();
        copySelectedFields(info, comPortPool, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComPortPoolInfo, ComPortPool>> buildFieldMap() {
        Map<String, PropertyCopier<ComPortPoolInfo,ComPortPool>> map = new HashMap<>();
        map.put("id", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.id = comPortPool.getId()));
        map.put("link", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.link = link(comPortPool, Relation.REF_SELF, uriInfo)));
        map.put("version", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.version = comPortPool.getVersion()));
        map.put("name", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.name = comPortPool.getName()));
        map.put("active", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.active = comPortPool.isActive()));
        map.put("description", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.description = comPortPool.getDescription()));
        map.put("type", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.type = comPortPool.getComPortType()));
        return map;
    }
}
