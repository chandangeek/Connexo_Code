package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
    public KpiResource(DataCollectionKpiService dataCollectionKpiService, DataCollectionKpiInfoFactory dataCollectionKpiInfoFactory, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.dataCollectionKpiInfoFactory = dataCollectionKpiInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllKpis(@BeanParam QueryParameters queryParameters) {
        List<DataCollectionKpiInfo> collection = dataCollectionKpiService.dataCollectionKpiFinder().
                from(queryParameters).defaultSortColumn(DataCollectionKpiImpl.Fields.END_DEVICE_GROUP + ".label").
                stream().
                map(dataCollectionKpiInfoFactory::from).
                collect(toList());

        return PagedInfoList.asJson("kpis", collection, queryParameters);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public DataCollectionKpiInfo getKpiById(@PathParam("id") long id) {
        DataCollectionKpi dataCollectionKpi = dataCollectionKpiService.findDataCollectionKpi(id).orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_KPI, id));
        return dataCollectionKpiInfoFactory.from(dataCollectionKpi);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response deleteKpi(@PathParam("id") long id) {
        dataCollectionKpiService.findDataCollectionKpi(id).orElseThrow(()->exceptionFactory.newException(MessageSeeds.NO_SUCH_KPI, id)).delete();
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DataCollectionKpiInfo createKpi(DataCollectionKpiInfo kpiInfo) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup((Long) kpiInfo.deviceGroup.id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, kpiInfo.deviceGroup.id));
        DataCollectionKpiService.DataCollectionKpiBuilder dataCollectionKpiBuilder = dataCollectionKpiService.newDataCollectionKpi(endDeviceGroup);
        if (kpiInfo.communicationTarget!=null) {
            dataCollectionKpiBuilder.calculateComTaskExecutionKpi(kpiInfo.frequency.every.asTemporalAmount()).expectingAsMaximum(kpiInfo.communicationTarget);
        }
        if (kpiInfo.connectionTarget!=null) {
            dataCollectionKpiBuilder.calculateConnectionSetupKpi(kpiInfo.frequency.every.asTemporalAmount()).expectingAsMaximum(kpiInfo.connectionTarget);
        }
        DataCollectionKpi dataCollectionKpi = dataCollectionKpiBuilder.save();
        return dataCollectionKpiInfoFactory.from(dataCollectionKpi);
    }


}
