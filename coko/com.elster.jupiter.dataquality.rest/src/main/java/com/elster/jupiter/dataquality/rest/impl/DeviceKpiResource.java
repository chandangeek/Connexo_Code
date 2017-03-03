/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.DeviceDataQualityKpiInfo;

@Path("/deviceKpis")
public class DeviceKpiResource {

    private final DataQualityKpiService dataQualityKpiService;
    private final DataQualityKpiInfoFactory dataQualityKpiInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceKpiResource(DataQualityKpiService dataQualityKpiService, DataQualityKpiInfoFactory dataQualityKpiInfoFactory, ResourceHelper resourceHelper) {
        this.dataQualityKpiService = dataQualityKpiService;
        this.dataQualityKpiInfoFactory = dataQualityKpiInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public PagedInfoList getAllDataQualityKpis(@BeanParam JsonQueryParameters queryParameters) {
        List<DeviceDataQualityKpiInfo> infos = dataQualityKpiService.deviceDataQualityKpiFinder()
                .from(queryParameters)
                .stream()
                .map(dataQualityKpiInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceKpis", infos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public DeviceDataQualityKpiInfo getDataQualityKpiById(@PathParam("id") long id) {
        DeviceDataQualityKpi dataQualityKpi = dataQualityKpiService.findDeviceDataQualityKpi(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return dataQualityKpiInfoFactory.from(dataQualityKpi);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response createDataQualityKpi(DeviceDataQualityKpiInfo info) {
        dataQualityKpiInfoFactory.createNewKpi(info);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response deleteDataQualityKpi(@PathParam("id") long id, DeviceDataQualityKpiInfo info) {
        info.id = id;
        resourceHelper.findAndLockDataQualityKpi(info).delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
