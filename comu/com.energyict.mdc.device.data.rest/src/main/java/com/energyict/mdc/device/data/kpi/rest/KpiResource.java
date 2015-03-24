package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
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

    @Inject
    public KpiResource(DataCollectionKpiService dataCollectionKpiService, DataCollectionKpiInfoFactory dataCollectionKpiInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus) {
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.dataCollectionKpiInfoFactory = dataCollectionKpiInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getAvailableDeviceGroups(@BeanParam QueryParameters queryParameters){
        List<EndDeviceGroup> allGroups = meteringGroupsService.getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
        List<Long> usedGroupIds = dataCollectionKpiService.findAllDataCollectionKpis().stream().map(kpi -> kpi.getDeviceGroup().getId()).collect(Collectors.toList());
        Iterator<EndDeviceGroup> groupIterator = allGroups.iterator();
        while (groupIterator.hasNext()) {
            EndDeviceGroup next =  groupIterator.next();
            if (usedGroupIds.contains(next.getId())){
                groupIterator.remove();
            }
        }
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", allGroups.stream()
                .map(gr -> new LongIdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PagedInfoList getAllKpis(@BeanParam QueryParameters queryParameters) {
        List<DataCollectionKpiInfo> collection = dataCollectionKpiService.dataCollectionKpiFinder().
                from(queryParameters).
                stream().
                map(dataCollectionKpiInfoFactory::from).
                collect(toList());

        return PagedInfoList.fromPagedList("kpis", collection, queryParameters);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    public DataCollectionKpiInfo getKpiById(@PathParam("id") long id) {
        DataCollectionKpi dataCollectionKpi = dataCollectionKpiService.findDataCollectionKpi(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KPI, id));
        return dataCollectionKpiInfoFactory.from(dataCollectionKpi);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    public Response deleteKpi(@PathParam("id") long id) {
        dataCollectionKpiService.findDataCollectionKpi(id).orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_KPI, id)).delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response createKpi(DataCollectionKpiInfo kpiInfo) {
        EndDeviceGroup endDeviceGroup=null;
        if (kpiInfo.deviceGroup != null && kpiInfo.deviceGroup.id!=null) {
            endDeviceGroup = meteringGroupsService.findEndDeviceGroup(kpiInfo.deviceGroup.id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, kpiInfo.deviceGroup.id));
        }
        DataCollectionKpiService.DataCollectionKpiBuilder dataCollectionKpiBuilder = dataCollectionKpiService.newDataCollectionKpi(endDeviceGroup);
        if (kpiInfo.frequency == null || kpiInfo.frequency.every == null){
            /* Send the correct validation error because if frequency is null we can't create connection or communication kpi -> FE receive unclear message */
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY, "frequency");
        }
        if (kpiInfo.displayRange != null){
            dataCollectionKpiBuilder.displayPeriod(kpiInfo.displayRange.asTimeDuration());
        }
        if (kpiInfo.communicationTarget!=null && kpiInfo.frequency !=null && kpiInfo.frequency.every!=null && kpiInfo.frequency.every.asTimeDuration()!=null) {
            dataCollectionKpiBuilder.calculateComTaskExecutionKpi(kpiInfo.frequency.every.asTimeDuration().asTemporalAmount()).expectingAsMaximum(kpiInfo.communicationTarget);
        }
        if (kpiInfo.connectionTarget!=null && kpiInfo.frequency !=null && kpiInfo.frequency.every!=null && kpiInfo.frequency.every.asTimeDuration()!=null) {
            dataCollectionKpiBuilder.calculateConnectionSetupKpi(kpiInfo.frequency.every.asTimeDuration().asTemporalAmount()).expectingAsMaximum(kpiInfo.connectionTarget);
        }

        DataCollectionKpi dataCollectionKpi = dataCollectionKpiBuilder.save();
        return Response.status(Response.Status.CREATED).entity(dataCollectionKpiInfoFactory.from(dataCollectionKpi)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    public Response updateKpi(@PathParam("id") long id, DataCollectionKpiInfo kpiInfo) {
        DataCollectionKpi kpi = dataCollectionKpiService.findDataCollectionKpi(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KPI, id));

        if (kpiInfo.communicationTarget!=null) {
            if (kpiInfo.frequency !=null && kpiInfo.frequency.every!=null) {
                if (!kpi.calculatesComTaskExecutionKpi() || (kpi.calculatesComTaskExecutionKpi() && (!kpiInfo.frequency.every.asTimeDuration().asTemporalAmount().equals(kpi.comTaskExecutionKpiCalculationIntervalLength().get()) ||
                        !kpiInfo.communicationTarget.equals(kpi.getStaticCommunicationKpiTarget().get())))) {
                    // something changed about communication KPI
                    kpi.calculateComTaskExecutionKpi(kpiInfo.communicationTarget);
                }
            }
        } else {
            // remove ComTaskExecutionKpi
            if (kpi.calculatesComTaskExecutionKpi()) {
                kpi.dropComTaskExecutionKpi();
            }
        }
        if (kpiInfo.connectionTarget!=null) {
            if (kpiInfo.frequency !=null && kpiInfo.frequency.every!=null) {
                if (!kpi.calculatesConnectionSetupKpi() || (kpi.calculatesConnectionSetupKpi() && (!kpiInfo.frequency.every.asTimeDuration().asTemporalAmount().equals(kpi.connectionSetupKpiCalculationIntervalLength().get()) ||
                        !kpiInfo.connectionTarget.equals(kpi.getStaticConnectionKpiTarget().get())))) {
                    // something changed about connection KPI
                    kpi.calculateConnectionKpi(kpiInfo.connectionTarget);
                }
            }
        } else {
            // drop connection kpi
            if (kpi.calculatesConnectionSetupKpi()) {
                kpi.dropConnectionSetupKpi();
            }
        }
        kpi.updateDisplayRange(kpiInfo.displayRange == null ? null : kpiInfo.displayRange.asTimeDuration());
        return Response.ok(dataCollectionKpiInfoFactory.from(dataCollectionKpiService.findDataCollectionKpi(id).get())).build();
    }


}
