package com.energyict.mdc.multisense.api.impl;


import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/14/15.
 */
public class ComPortPoolInfoFactory extends SelectableFieldFactory<ComPortPoolInfo, ComPortPool> {

    public ComPortPoolInfo asHypermedia(ComPortPool comPortPool, UriInfo uriInfo, Collection<String> fields) {
        ComPortPoolInfo info = new ComPortPoolInfo();
        copySelectedFields(info, comPortPool, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComPortPoolInfo, ComPortPool>> buildFieldMap() {
        Map<String, PropertyCopier<ComPortPoolInfo,ComPortPool>> map = new HashMap<>();
        map.put("id", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.id = comPortPool.getId()));
        map.put("link", ((comPortPoolInfo, comPortPool, uriInfo) ->
            comPortPoolInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(ComPortPoolResource.class).
                    path(ComPortPoolResource.class, "getComPortPool")).
                    rel(LinkInfo.REF_SELF).
                    title("com port pool").
                    build(comPortPool.getId())
        ));
        map.put("name", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.name = comPortPool.getName()));
        map.put("active", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.active = comPortPool.isActive()));
        map.put("description", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.description = comPortPool.getDescription()));
        map.put("type", ((comPortPoolInfo, comPortPool, uriInfo) -> comPortPoolInfo.type = comPortPool.getComPortType()));
        return map;
    }
}
