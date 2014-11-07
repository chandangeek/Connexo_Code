package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.rest.DataExportTaskInfo;
import com.elster.jupiter.export.rest.DataExportTaskInfos;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/dataexporttask")
public class DataExportTaskResource {

    private final DataExportService dataExportService;
    private final RestQueryService queryService;
    private final TimeService timeService;
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;

    @Inject
    public DataExportTaskResource(RestQueryService queryService, DataExportService dataExportService, TimeService timeService, MeteringService meteringService, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus, TransactionService transactionService) {
        this.queryService = queryService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ReadingTypeDataExportTask> list = queryTasks(params);

        DataExportTaskInfos infos = new DataExportTaskInfos(params.clipToLimit(list), thesaurus);
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<? extends ReadingTypeDataExportTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends ReadingTypeDataExportTask> query = dataExportService.getReadingTypeDataExportTaskQuery();
        RestQuery<? extends ReadingTypeDataExportTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new DataExportTaskInfo(fetchDataExportTask(id, securityContext), thesaurus);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReadingTypeDataExportTask(DataExportTaskInfo info) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(info.name)
                .setDataProcessorName(info.dataProcessor)
                .setScheduleExpression(info.schedule == null ? Never.NEVER : info.schedule.asScheduleExpression())
                .setExportPeriod(getRelativePeriod(info.exportperiod))
                .setUpdatePeriod(getRelativePeriod(info.updatePeriod))
                .setValidatedDataOption(info.validatedDataOption)
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id))
                .exportContinuousData(info.exportContinuousData)
                .exportUpdate(info.exportUpdate);

        List<PropertySpec<?>> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor);
        PropertyUtils propertyUtils = new PropertyUtils();

        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });

        info.readingTypes.stream()
                .forEach(r -> {
                    meteringService.getReadingType(r.mRID).ifPresent(builder::addReadingType);
                });

        ReadingTypeDataExportTask dataExportTask = builder.build();
        try (TransactionContext context = transactionService.getContext()) {
            dataExportTask.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(dataExportTask, thesaurus)).build();
    }


    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private RelativePeriod getRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        if (relativePeriodInfo == null) {
            return null;
        }
        return timeService.findRelativePeriod(relativePeriodInfo.id).orElse(null);
    }

    private ReadingTypeDataExportTask fetchDataExportTask(long id, SecurityContext securityContext) {
        return dataExportService.findExportTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
