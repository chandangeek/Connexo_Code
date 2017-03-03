/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldResource {

    private final DataQualityKpiService dataQualityKpiService;

    @Inject
    public FieldResource(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @GET
    @Transactional
    @Path("/kpiDeviceGroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public Response getDeviceGroupsWithDataQualityKpi(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = dataQualityKpiService.deviceDataQualityKpiFinder()
                .stream()
                .filter(kpi -> kpi.getLatestCalculation().isPresent())
                .map(DeviceDataQualityKpi::getDeviceGroup)
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", infos, queryParameters)).build();
    }
}
