package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.rest.DataExportTaskInfo;
import com.elster.jupiter.export.rest.DataExportTaskInfos;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
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
                .setScheduleExpression(getScheduleExpression(info))
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
                .map(r -> r.mRID)
                .forEach(builder::addReadingType);

        ReadingTypeDataExportTask dataExportTask = builder.build();
        try (TransactionContext context = transactionService.getContext()) {
            dataExportTask.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(dataExportTask, thesaurus)).build();
    }

    @DELETE
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ReadingTypeDataExportTask task = fetchDataExportTask(id, securityContext);

        if (task.getLastOccurence().isPresent() && task.getLastOccurence().get().getStatus().equals(DataExportStatus.BUSY)) {
           throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_STATUS_BUSY, "status");
        }
        try (TransactionContext context = transactionService.getContext()) {
            task.delete();
            context.commit();
        } catch (CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", thesaurus.getStringBeyondComponent(task.getName(), task.getName()));
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReadingTypeDataExportTask(DataExportTaskInfo info) {

        ReadingTypeDataExportTask task = findTaskOrThrowException(info);

        try (TransactionContext context = transactionService.getContext()) {
            task.setName(info.name);
            task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
            task.setScheduleExpression(getScheduleExpression(info));
            task.setExportPeriod(getRelativePeriod(info.exportperiod));
            task.setUpdatePeriod(getRelativePeriod(info.updatePeriod));
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));

            updateProperties(info, task);
            updateReadingTypes(info, task);

            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(task, thesaurus)).build();
    }

    private ReadingTypeDataExportTask findTaskOrThrowException(DataExportTaskInfo info) {
        return dataExportService.findExportTask(info.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void updateReadingTypes(DataExportTaskInfo info, ReadingTypeDataExportTask task) {
        info.readingTypes.stream()
                .map(r -> r.mRID)
                .filter(m -> task.getReadingTypes().stream().map(ReadingType::getMRID).noneMatch(s -> s.equals(m)))
                .forEach(task::addReadingType);
        task.getReadingTypes().stream()
                .filter(t -> info.readingTypes.stream().map(r -> r.mRID).anyMatch(m -> t.getMRID().equals(m)))
                .forEach(task::removeReadingType);
    }

    private void updateProperties(DataExportTaskInfo info, ReadingTypeDataExportTask task) {
        List<PropertySpec<?>> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor);
        PropertyUtils propertyUtils = new PropertyUtils();
        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    task.setProperty(spec.getName(), value);
                });
    }

    private ScheduleExpression getScheduleExpression(DataExportTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.asScheduleExpression();
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
