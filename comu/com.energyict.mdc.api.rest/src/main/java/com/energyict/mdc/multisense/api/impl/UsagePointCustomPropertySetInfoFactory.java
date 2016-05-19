package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.rest.util.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.rest.util.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class UsagePointCustomPropertySetInfoFactory extends SelectableFieldFactory<UsagePointCustomPropertySetInfo, UsagePointPropertySet> {

    private final Clock clock;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Inject
    public UsagePointCustomPropertySetInfoFactory(Clock clock, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.clock = clock;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    public LinkInfo asLink(UsagePointPropertySet usagePointCustomPropertySet, Relation relation, UriInfo uriInfo) {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        copySelectedFields(info, usagePointCustomPropertySet, uriInfo, Arrays.asList("id", "version"));
        info.link = link(usagePointCustomPropertySet, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<UsagePointPropertySet> usagePointCustomPropertySets, Relation relation, UriInfo uriInfo) {
        return usagePointCustomPropertySets.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(UsagePointPropertySet usagePointCustomPropertySet, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Custom property set")
                .build(usagePointCustomPropertySet.getUsagePoint().getId(), usagePointCustomPropertySet.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(UsagePointCustomPropertySetResource.class)
                .path(UsagePointCustomPropertySetResource.class, "getUsagePointCustomPropertySet");
    }

    public UsagePointCustomPropertySetInfo from(UsagePointPropertySet usagePointCustomPropertySet, UriInfo uriInfo, Collection<String> fields) {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        copySelectedFields(info, usagePointCustomPropertySet, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<UsagePointCustomPropertySetInfo, UsagePointPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<UsagePointCustomPropertySetInfo, UsagePointPropertySet>> map = new HashMap<>();
        map.put("id", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.id = usagePointCustomPropertySet
                .getId());
        map.put("link", ((usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) ->
                usagePointCustomPropertySetInfo.link = link(usagePointCustomPropertySet, Relation.REF_SELF, uriInfo)));
        map.put("version", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) ->
                usagePointCustomPropertySetInfo.version = usagePointCustomPropertySet.getUsagePoint().getVersion());
        map.put("name", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.name = usagePointCustomPropertySet
                .getCustomPropertySet()
                .getName());
        map.put("domainDomainName", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.domainClass = usagePointCustomPropertySet
                .getCustomPropertySet()
                .getDomainClass()
                .getName());
        map.put("isRequired", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.isRequired = usagePointCustomPropertySet
                .getCustomPropertySet()
                .isRequired());
        map.put("isVersioned", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> usagePointCustomPropertySetInfo.isVersioned = usagePointCustomPropertySet
                .getCustomPropertySet()
                .isVersioned());
        map.put("isActive", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.isActive = !usagePointCustomPropertySet.getValues().isEmpty() &&
                        usagePointCustomPropertySet.getValues().getEffectiveRange().contains(clock.instant());
            }
        });
        map.put("startTime", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.startTime = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasLowerBound() ?
                        usagePointCustomPropertySet.getValues()
                                .getEffectiveRange()
                                .lowerEndpoint()
                                .toEpochMilli() : null;
            }
        });
        map.put("endTime", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.endTime = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasUpperBound() ?
                        usagePointCustomPropertySet.getValues()
                                .getEffectiveRange()
                                .upperEndpoint()
                                .toEpochMilli() : null;
            }
        });
        map.put("versionId", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            if (usagePointCustomPropertySet.getCustomPropertySet()
                    .isVersioned() && usagePointCustomPropertySet.getValues() != null) {
                usagePointCustomPropertySetInfo.versionId = usagePointCustomPropertySet.getValues()
                        .getEffectiveRange()
                        .hasLowerBound() ?
                        usagePointCustomPropertySet.getValues().getEffectiveRange().lowerEndpoint().toEpochMilli() : 0L;
            }
        });
        map.put("properties", (usagePointCustomPropertySetInfo, usagePointCustomPropertySet, uriInfo) -> {
            CustomPropertySet<UsagePoint, ?> customPropertySet = usagePointCustomPropertySet.getCustomPropertySet();

            usagePointCustomPropertySetInfo.properties =
                    customPropertySet.getPropertySpecs().stream()
                            .map(cps -> customPropertySetInfoFactory.getPropertyInfo(cps, name -> usagePointCustomPropertySet
                                    .getValues() != null ? usagePointCustomPropertySet.getValues()
                                    .getProperty(name) : null))
                            .collect(toList());
        });
        return map;
    }

    public CustomPropertySetValues getValues(UsagePointCustomPropertySetInfo propertySetInfo, UsagePointPropertySet propertySet) {
        CustomPropertySetInfo customPropertySetInfo = new CustomPropertySetInfo();
        customPropertySetInfo.isVersioned = propertySetInfo.isVersioned;
        customPropertySetInfo.startTime = propertySetInfo.startTime;
        customPropertySetInfo.endTime = propertySetInfo.endTime;
        customPropertySetInfo.properties = propertySetInfo.properties;

        return customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo, propertySet.getCustomPropertySet()
                .getPropertySpecs());
    }

}
