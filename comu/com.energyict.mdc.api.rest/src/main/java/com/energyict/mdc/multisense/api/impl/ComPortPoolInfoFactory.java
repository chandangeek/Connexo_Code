package com.energyict.mdc.multisense.api.impl;


import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
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
        return asLink(comPortPool, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ComPortPool> comPortPools, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return comPortPools.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ComPortPool comPortPool, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = comPortPool.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Communication port pool")
                .build(comPortPool.getId());
        return info;
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
        map.put("link", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.link = this.asLink(comPortPool, Relation.REF_SELF, uriInfo).link));
        map.put("name", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.name = comPortPool.getName()));
        map.put("active", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.active = comPortPool.isActive()));
        map.put("description", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.description = comPortPool.getDescription()));
        map.put("type", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.type = comPortPool.getComPortType()));
        return map;
    }
}
