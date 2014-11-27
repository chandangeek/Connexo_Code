package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
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
        return restQuery.select(queryParameters, Order.ascending("lastRun"));
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new DataExportTaskInfo(fetchDataExportTask(id, securityContext), thesaurus);
    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public Response triggerDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        transactionService.execute(VoidTransaction.of(() -> fetchDataExportTask(id, securityContext).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response addReadingTypeDataExportTask(DataExportTaskInfo info) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(info.name)
                .setDataProcessorName(info.dataProcessor.name)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun))
                .setExportPeriod(getRelativePeriod(info.exportperiod))
                .setUpdatePeriod(getRelativePeriod(info.updatePeriod))
                .setValidatedDataOption(info.validatedDataOption)
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id))
                .exportContinuousData(info.exportContinuousData)
                .exportUpdate(info.exportUpdate);

        List<PropertySpec<?>> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor.name);
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
    @RolesAllowed(Privileges.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response removeDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ReadingTypeDataExportTask task = fetchDataExportTask(id, securityContext);

        if (!task.canBeDeleted()) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_STATUS_BUSY, "status");
        }
        try (TransactionContext context = transactionService.getContext()) {
            task.delete();
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", task.getName());
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK})
    public Response updateReadingTypeDataExportTask(@PathParam("id") long id, DataExportTaskInfo info) {

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

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportTaskHistoryInfos getDataExportTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                               @QueryParam("filter") JSONArray filterArray, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        Map<String, Long> filter = getFilterMap(filterArray);
        ReadingTypeDataExportTask task = fetchDataExportTask(id, securityContext);
        DataExportOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStart())
                .setLimit(queryParameters.getLimit());

        if (filter.get("startedOnFrom") != null && filter.get("startedOnTo") != null) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.ofEpochMilli(filter.get("startedOnFrom")), Instant.ofEpochMilli(filter.get("startedOnTo"))));
        }
        if (filter.get("finishedOnFrom") != null && filter.get("finishedOnTo") != null) {
            occurrencesFinder.withEndDateIn(Range.closed(Instant.ofEpochMilli(filter.get("finishedOnFrom")), Instant.ofEpochMilli(filter.get("finishedOnTo"))));
        }
        if (filter.get("exportPeriodContains") != null) {
            occurrencesFinder.withExportPeriodContaining(Instant.ofEpochMilli(filter.get("exportPeriodContains")));
        }

        List<? extends DataExportOccurrence> occurrences = occurrencesFinder.find();

        DataExportTaskHistoryInfos infos = new DataExportTaskHistoryInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    @GET
    @Path("/{id}/datasources")
    @Produces(MediaType.APPLICATION_JSON)
    public DataSourceInfos getDataSources(@PathParam("id") long id, @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        ReadingTypeDataExportTask task = fetchDataExportTask(id, securityContext);
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ReadingTypeDataExportItem> exportItems =
            ListPager.of(task.getExportItems()).paged(queryParameters.getStart(), queryParameters.getLimit()).find();
        return new DataSourceInfos(exportItems);
    }


    @GET
    @Path("/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataExportOccurrenceLogInfos getDataExportTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                                 @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        ReadingTypeDataExportTask task = fetchDataExportTask(id, securityContext);
        DataExportOccurrence occurrence = fetchDataExportOccurrence(occurrenceId, task, securityContext);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStart())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        DataExportOccurrenceLogInfos infos = new DataExportOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }


    private Map<String, Long> getFilterMap(JSONArray filterArray) {
        Map<String, Long> filterMap = new HashMap<>();
        if (filterArray != null) {
            for (int i = 0; i < filterArray.length(); i++) {
                try {
                    JSONObject object = filterArray.getJSONObject(i);
                    filterMap.put(object.getString("property"), Long.valueOf(object.get("value").toString()));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return filterMap;
    }

    private ReadingTypeDataExportTask findTaskOrThrowException(DataExportTaskInfo info) {
        return dataExportService.findExportTask(info.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void updateReadingTypes(DataExportTaskInfo info, ReadingTypeDataExportTask task) {
        task.getReadingTypes().stream()
                .filter(t -> info.readingTypes.stream().map(r -> r.mRID).anyMatch(m -> t.getMRID().equals(m)))
                .forEach(task::removeReadingType);
        info.readingTypes.stream()
                .map(r -> r.mRID)
                .filter(m -> task.getReadingTypes().stream().map(ReadingType::getMRID).noneMatch(s -> s.equals(m)))
                .forEach(task::addReadingType);
    }

    private void updateProperties(DataExportTaskInfo info, ReadingTypeDataExportTask task) {
        List<PropertySpec<?>> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor.name);
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

    private DataExportOccurrence fetchDataExportOccurrence(long id, ReadingTypeDataExportTask task, SecurityContext securityContext) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
