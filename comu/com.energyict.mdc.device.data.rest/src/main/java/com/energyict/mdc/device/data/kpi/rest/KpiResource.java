/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.rest.impl.ResourceHelper;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 12/12/14.
 */
@Path("/kpis")
public class KpiResource {

    private final DataCollectionKpiService dataCollectionKpiService;
    private final DataCollectionKpiInfoFactory dataCollectionKpiInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ResourceHelper resourceHelper;

    @Inject
    public KpiResource(DataCollectionKpiService dataCollectionKpiService, DataCollectionKpiInfoFactory dataCollectionKpiInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus, ResourceHelper resourceHelper) {
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.dataCollectionKpiInfoFactory = dataCollectionKpiInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_COLLECTION_KPI, Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public Response getAvailableDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<EndDeviceGroup> allGroups = meteringGroupsService.getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
        List<Long> usedGroupIds =
                dataCollectionKpiService
                        .findAllDataCollectionKpis()
                        .stream()
                        .map(DataCollectionKpi::getDeviceGroup)
                        .map(HasId::getId)
                        .collect(Collectors.toList());
        Iterator<EndDeviceGroup> groupIterator = allGroups.iterator();
        while (groupIterator.hasNext()) {
            EndDeviceGroup next = groupIterator.next();
            if (usedGroupIds.contains(next.getId())) {
                groupIterator.remove();
            }
        }
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", allGroups.stream()
                .map(gr -> new LongIdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_COLLECTION_KPI, Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public PagedInfoList getAllKpis(@BeanParam JsonQueryParameters queryParameters) {
        List<DataCollectionKpiInfo> collection = dataCollectionKpiService.dataCollectionKpiFinder().
                from(queryParameters).
                stream().
                map(dataCollectionKpiInfoFactory::from).
                collect(toList());

        return PagedInfoList.fromPagedList("kpis", collection, queryParameters);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_COLLECTION_KPI, Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public DataCollectionKpiInfo getKpiById(@PathParam("id") long id) {
        DataCollectionKpi dataCollectionKpi = resourceHelper.findDataCollectionKpiByIdOrThrowException(id);
        return dataCollectionKpiInfoFactory.from(dataCollectionKpi);
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public Response deleteKpi(@PathParam("id") long id, DataCollectionKpiInfo info) {
        info.id = id;
        resourceHelper.lockDataCollectionKpiOrThrowException(info).delete();
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public Response createKpi(DataCollectionKpiInfo kpiInfo) {
        EndDeviceGroup endDeviceGroup = null;
        if (kpiInfo.deviceGroup != null && kpiInfo.deviceGroup.id != null) {
            endDeviceGroup = meteringGroupsService.findEndDeviceGroup(kpiInfo.deviceGroup.id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, kpiInfo.deviceGroup.id));
        }
        DataCollectionKpiService.DataCollectionKpiBuilder dataCollectionKpiBuilder = dataCollectionKpiService.newDataCollectionKpi(endDeviceGroup);
        if (kpiInfo.frequency != null && kpiInfo.frequency.every != null && kpiInfo.frequency.every.asTimeDuration() != null) {
            dataCollectionKpiBuilder.frequency(kpiInfo.frequency.every.asTimeDuration().asTemporalAmount());
        }
        if (kpiInfo.displayRange != null) {
            dataCollectionKpiBuilder.displayPeriod(kpiInfo.displayRange.asTimeDuration());
        }
        if (kpiInfo.communicationTarget != null) {
            dataCollectionKpiBuilder.calculateComTaskExecutionKpi().expectingAsMaximum(kpiInfo.communicationTarget);
        }
        if (kpiInfo.connectionTarget != null) {
            dataCollectionKpiBuilder.calculateConnectionSetupKpi().expectingAsMaximum(kpiInfo.connectionTarget);
        }

        DataCollectionKpi dataCollectionKpi = dataCollectionKpiBuilder.save();
        return Response.status(Response.Status.CREATED).entity(dataCollectionKpiInfoFactory.from(dataCollectionKpi)).build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public Response updateKpi(@PathParam("id") long id, DataCollectionKpiInfo info) {
        info.id = id;
        DataCollectionKpi kpi = resourceHelper.lockDataCollectionKpiOrThrowException(info);

        if (info.communicationTarget != null) {
            if (info.frequency != null && info.frequency.every != null) {
                if (!kpi.calculatesComTaskExecutionKpi() || (kpi.calculatesComTaskExecutionKpi() && (!info.frequency.every.asTimeDuration().asTemporalAmount().equals(kpi.comTaskExecutionKpiCalculationIntervalLength().get()) ||
                        !info.communicationTarget.equals(kpi.getStaticCommunicationKpiTarget().get())))) {
                    // something changed about communication KPI
                    kpi.calculateComTaskExecutionKpi(info.communicationTarget);
                }
            }
        }
        if (info.connectionTarget != null) {
            if (info.frequency != null && info.frequency.every != null) {
                if (!kpi.calculatesConnectionSetupKpi() || (kpi.calculatesConnectionSetupKpi() && (!info.frequency.every.asTimeDuration().asTemporalAmount().equals(kpi.connectionSetupKpiCalculationIntervalLength().get()) ||
                        !info.connectionTarget.equals(kpi.getStaticConnectionKpiTarget().get())))) {
                    // something changed about connection KPI
                    kpi.calculateConnectionKpi(info.connectionTarget);
                }
            }
        }
        if (info.connectionTarget == null) {
            // drop connection kpi
            if (kpi.calculatesConnectionSetupKpi()) {
                kpi.dropConnectionSetupKpi();
            }
        }
        if (info.communicationTarget == null) {
            // remove ComTaskExecutionKpi
            if (kpi.calculatesComTaskExecutionKpi()) {
                kpi.dropComTaskExecutionKpi();
            }
        }
        kpi.updateDisplayRange(info.displayRange == null ? null : info.displayRange.asTimeDuration());
        return Response.ok(dataCollectionKpiInfoFactory.from(dataCollectionKpiService.findDataCollectionKpi(id).get())).build();
    }
}
