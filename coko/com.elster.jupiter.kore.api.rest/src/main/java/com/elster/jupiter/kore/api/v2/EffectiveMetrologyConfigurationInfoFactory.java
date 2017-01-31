/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.inject.Provider;
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

public class EffectiveMetrologyConfigurationInfoFactory extends SelectableFieldFactory<EffectiveMetrologyConfigurationInfo, EffectiveMetrologyConfigurationOnUsagePoint> {


    private final Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory;
    private final Provider<MetrologyConfigurationPurposeInfoFactory> metrologyConfigurationPurposeInfoFactory;

    @Inject
    public EffectiveMetrologyConfigurationInfoFactory(Provider<MetrologyConfigurationInfoFactory> metrologyConfigurationInfoFactory, Provider<MetrologyConfigurationPurposeInfoFactory> metrologyConfigurationPurposeInfoFactory) {
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationPurposeInfoFactory = metrologyConfigurationPurposeInfoFactory;
    }

    public LinkInfo asLink(EffectiveMetrologyConfigurationOnUsagePoint metrology, Relation relation, UriInfo uriInfo) {
        EffectiveMetrologyConfigurationInfo info = new EffectiveMetrologyConfigurationInfo();
        copySelectedFields(info, metrology, uriInfo, Arrays.asList("id", "version"));
        info.link = link(metrology, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<EffectiveMetrologyConfigurationOnUsagePoint> metrologys, Relation relation, UriInfo uriInfo) {
        return metrologys.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(EffectiveMetrologyConfigurationOnUsagePoint metrology, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Metrology configuration")
                .build(metrology.getUsagePoint().getMRID(), metrology.getRange().hasLowerBound() ? metrology.getRange().lowerEndpoint().toEpochMilli() : 0);
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(EffectiveMetrologyConfigurationResource.class)
                .path(EffectiveMetrologyConfigurationResource.class, "getMetrologyConfiguration");
    }

    public EffectiveMetrologyConfigurationInfo from(EffectiveMetrologyConfigurationOnUsagePoint metrology, UriInfo uriInfo, Collection<String> fields) {
        EffectiveMetrologyConfigurationInfo info = new EffectiveMetrologyConfigurationInfo();
        copySelectedFields(info, metrology, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<EffectiveMetrologyConfigurationInfo, EffectiveMetrologyConfigurationOnUsagePoint>> buildFieldMap() {
        Map<String, PropertyCopier<EffectiveMetrologyConfigurationInfo, EffectiveMetrologyConfigurationOnUsagePoint>> map = new HashMap<>();
        map.put("id", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.id = metrology.getRange()
                .hasLowerBound() ? metrology.getRange().lowerEndpoint().toEpochMilli() : 0);
        map.put("link", ((metrologyInfo, metrology, uriInfo) ->
                metrologyInfo.link = link(metrology, Relation.REF_SELF, uriInfo)));
        map.put("metrologyConfiguration", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.metrologyConfiguration = metrologyConfigurationInfoFactory
                .get()
                .asLink(metrology.getMetrologyConfiguration(), Relation.REF_RELATION, uriInfo));
        map.put("purposes", (metrologyInfo, metrology, uriInfo) -> metrologyInfo.purposes = metrology.getMetrologyConfiguration()
                .getContracts().stream().map(c -> metrologyConfigurationPurposeInfoFactory.get().asInfo(
                        c.getId(),
                        c.getMetrologyPurpose().getName(),
                        c.isMandatory(),
                        c.getStatus(metrology.getUsagePoint()).getKey())).collect(Collectors.toList()));
        return map;
    }
}
