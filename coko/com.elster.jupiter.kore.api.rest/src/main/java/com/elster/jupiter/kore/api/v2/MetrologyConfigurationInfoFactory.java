/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class MetrologyConfigurationInfoFactory extends SelectableFieldFactory<MetrologyConfigurationInfo, MetrologyConfiguration> {

    public LinkInfo asLink(MetrologyConfiguration metrology, Relation relation, UriInfo uriInfo) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        copySelectedFields(info, metrology, uriInfo, Arrays.asList("id", "version"));
        info.link = link(metrology, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<MetrologyConfiguration> metrologys, Relation relation, UriInfo uriInfo) {
        return metrologys.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(MetrologyConfiguration metrology, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Metrology configuration")
                .build(metrology.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(MetrologyConfigurationResource.class)
                .path(MetrologyConfigurationResource.class, "getMetrologyConfiguration");
    }

    public MetrologyConfigurationInfo from(MetrologyConfiguration metrology, UriInfo uriInfo, Collection<String> fields) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        copySelectedFields(info, metrology, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<MetrologyConfigurationInfo, MetrologyConfiguration>> buildFieldMap() {
        Map<String, PropertyCopier<MetrologyConfigurationInfo, MetrologyConfiguration>> map = new HashMap<>();
        map.put("id", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.id = metrology.getId());
        map.put("version", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.version = metrology.getVersion());
        map.put("link", ((metrologyInfo, metrology, uriInfo) ->
                metrologyInfo.link = link(metrology, Relation.REF_SELF, uriInfo)));
        map.put("name", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.name = metrology.getName());
        map.put("active", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.active = metrology.isActive());
        map.put("userName", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.userName = metrology.getUserName());
        map.put("createTime", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.createTime = metrology.getCreateTime());
        map.put("modTime", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.modTime = metrology.getModTime());
        map.put("meterRoles", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.meterRoles
                = ((UsagePointMetrologyConfiguration) metrology).getMeterRoles()
                .stream()
                .map(MeterRole::getKey)
                .collect(Collectors.toList()));
        return map;
    }
}
