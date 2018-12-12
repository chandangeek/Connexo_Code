/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@Path("/fields")
public class FieldResource {

    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final DataQualityKpiService dataQualityKpiService;

    @Inject
    public FieldResource(MeteringGroupsService meteringGroupsService, MetrologyConfigurationService metrologyConfigurationService, DataQualityKpiService dataQualityKpiService) {
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @GET
    @Path("/deviceGroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response getAvailableDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        Set<EndDeviceGroup> usedGroups = dataQualityKpiService.deviceDataQualityKpiFinder().stream()
                .map(DeviceDataQualityKpi::getDeviceGroup)
                .collect(Collectors.toSet());
        List<IdWithNameInfo> infos = meteringGroupsService.getEndDeviceGroupQuery()
                .select(Condition.TRUE, Order.ascending("upper(name)"))
                .stream()
                .filter(not(usedGroups::contains))
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", infos, queryParameters)).build();
    }

    @GET
    @Path("/usagePointGroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public PagedInfoList getAvailableUsagePointGroupsWithPurposes(@BeanParam JsonQueryParameters queryParameters) {
        List<MetrologyPurpose> metrologyPurposes = metrologyConfigurationService.getMetrologyPurposes();

        Multimap<UsagePointGroup, MetrologyPurpose> availableGroups = HashMultimap.create();
        meteringGroupsService.getUsagePointGroupQuery().select(Condition.TRUE).stream()
                .forEach(group -> availableGroups.putAll(group, metrologyPurposes));

        dataQualityKpiService.usagePointDataQualityKpiFinder().stream()
                .forEach(kpi -> availableGroups.remove(kpi.getUsagePointGroup(), kpi.getMetrologyPurpose()));

        List<AvailableUsagePointGroup> infos = availableGroups.asMap().entrySet()
                .stream()
                .map(entry -> new AvailableUsagePointGroup(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(info -> info.name.toUpperCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("usagePointGroups", infos, queryParameters);
    }

    private static class AvailableUsagePointGroup extends IdWithNameInfo {

        public IdWithNameInfo[] purposes;

        public AvailableUsagePointGroup(UsagePointGroup usagePointGroup, Collection<MetrologyPurpose> metrologyPurposes) {
            super(usagePointGroup);
            this.purposes = metrologyPurposes.stream()
                    .map(IdWithNameInfo::new)
                    .sorted(Comparator.comparing(info -> info.name.toUpperCase()))
                    .toArray(IdWithNameInfo[]::new);
        }
    }
}
