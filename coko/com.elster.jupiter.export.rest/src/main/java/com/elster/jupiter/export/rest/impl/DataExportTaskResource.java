package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryFilter;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/dataexporttask")
public class DataExportTaskResource {

    private final DataExportService dataExportService;
    private final RestQueryService queryService;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final PropertyUtils propertyUtils;

    @Inject
    public DataExportTaskResource(RestQueryService queryService, DataExportService dataExportService, TimeService timeService, MeteringGroupsService meteringGroupsService, Thesaurus thesaurus, TransactionService transactionService, PropertyUtils propertyUtils) {
        this.queryService = queryService;
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public DataExportTaskInfos getDataExportTasks(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ExportTask> list = queryTasks(params);

        DataExportTaskInfos infos = new DataExportTaskInfos(params.clipToLimit(list), thesaurus, timeService, propertyUtils);
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<? extends ExportTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends ExportTask> query = dataExportService.getReadingTypeDataExportTaskQuery();
        RestQuery<? extends ExportTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.descending("lastRun").nullsLast());
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new DataExportTaskInfo(fetchDataExportTask(id), thesaurus, timeService, propertyUtils);
    }

    @POST
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DATA_EXPORT_TASK, Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK})
    public Response triggerDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        transactionService.execute(VoidTransaction.of(() -> fetchDataExportTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response addExportTask(DataExportTaskInfo info) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(info.name)
                .setDataProcessorName(info.dataProcessor.name)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));

        if (info.standardDataSelector == null) {
            builder.selectingCustom(info.dataSelector.name).endSelection();
            List<PropertySpec> propertiesSpecsForDataSelector = dataExportService.getPropertiesSpecsForDataSelector(info.dataSelector.name);

            propertiesSpecsForDataSelector.stream()
                    .forEach(spec -> {
                        Object value = propertyUtils.findPropertyValue(spec, info.properties);
                        builder.addProperty(spec.getName()).withValue(value);
                    });
        } else {
            DataExportTaskBuilder.StandardSelectorBuilder selectorBuilder = builder.selectingStandard()
                    .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                    .fromUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                    .withValidatedDataOption(info.standardDataSelector.validatedDataOption)
                    .fromEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id))
                    .continuousData(info.standardDataSelector.exportContinuousData)
                    .exportUpdate(info.standardDataSelector.exportUpdate);
            info.standardDataSelector.readingTypes.stream()
                    .map(r -> r.mRID)
                    .forEach(selectorBuilder::fromReadingType);
            selectorBuilder.endSelection();
        }

        List<PropertySpec> propertiesSpecsForProcessor = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor.name);

        propertiesSpecsForProcessor.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });



        ExportTask dataExportTask = builder.build();
        try (TransactionContext context = transactionService.getContext()) {
            dataExportTask.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(dataExportTask, thesaurus, timeService, propertyUtils)).build();
    }

    @DELETE
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response removeDataExportTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        ExportTask task = fetchDataExportTask(id);

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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK})
    public Response updateExportTask(@PathParam("id") long id, DataExportTaskInfo info) {

        ExportTask task = findTaskOrThrowException(info);

        try (TransactionContext context = transactionService.getContext()) {
            task.setName(info.name);
            task.setScheduleExpression(getScheduleExpression(info));
            if (Never.NEVER.equals(task.getScheduleExpression())) {
                task.setNextExecution(null);
            } else {
                task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
            }
            if (info.standardDataSelector != null) {
                ReadingTypeDataSelector selector = task.getReadingTypeDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
                selector.setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod));
                selector.setUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod));
                selector.setEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id));
                selector.save();
            }

            updateProperties(info, task);
            updateReadingTypes(info, task);

            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new DataExportTaskInfo(task, thesaurus, timeService, propertyUtils)).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public DataExportTaskHistoryInfos getDataExportTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                               @BeanParam JsonQueryFilter filter, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        ExportTask task = fetchDataExportTask(id);
        DataExportOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit() + 1);

        if (filter.hasProperty("startedOnFrom")) {
            occurrencesFinder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"),
                    filter.hasProperty("startedOnTo") ? filter.getInstant("startedOnTo") : Instant.now()));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            occurrencesFinder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"),
                    filter.hasProperty("finishedOnTo") ? filter.getInstant("finishedOnTo") : Instant.now()));
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }
        if (filter.hasProperty("exportPeriodContains")) {
            occurrencesFinder.withExportPeriodContaining(filter.getInstant("exportPeriodContains"));
        }

        List<? extends DataExportOccurrence> occurrences = occurrencesFinder.find();

        DataExportTaskHistoryInfos infos = new DataExportTaskHistoryInfos(task, queryParameters.clipToLimit(occurrences), thesaurus, timeService, propertyUtils);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    @GET
    @Path("/{id}/datasources")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public DataSourceInfos getDataSources(@PathParam("id") long id, @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        ExportTask task = fetchDataExportTask(id);
        return task.getReadingTypeDataSelector()
                .map(readingTypeDataSelector -> buildDataSourceInfos(readingTypeDataSelector, uriInfo))
                .orElse(new DataSourceInfos(Collections.emptyList()));
    }

    private DataSourceInfos buildDataSourceInfos(ReadingTypeDataSelector readingTypeDataSelector, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends ReadingTypeDataExportItem> allExportItems = readingTypeDataSelector.getExportItems();
        List<ReadingTypeDataExportItem> activeExportItems = new ArrayList<>();
        for (ReadingTypeDataExportItem item : allExportItems) {
            if (item.isActive()) {
                activeExportItems.add(item);
            }
        }
        List<? extends ReadingTypeDataExportItem> exportItems = ListPager.of(activeExportItems).paged(queryParameters.getStartInt(), queryParameters.getLimit()).find();
        DataSourceInfos dataSourceInfos = new DataSourceInfos(exportItems.subList(0, Math.min(queryParameters.getLimit(), exportItems.size())));
        dataSourceInfos.total = activeExportItems.size();

        return dataSourceInfos;
    }


    @GET
    @Path("/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public DataExportOccurrenceLogInfos getDataExportTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                                 @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        ExportTask task = fetchDataExportTask(id);
        DataExportOccurrence occurrence = fetchDataExportOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        DataExportOccurrenceLogInfos infos = new DataExportOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    private ExportTask findTaskOrThrowException(DataExportTaskInfo info) {
        return dataExportService.findExportTask(info.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void updateReadingTypes(DataExportTaskInfo info, ExportTask task) {
        ReadingTypeDataSelector selector = task.getReadingTypeDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        selector.getReadingTypes().stream()
                .filter(t -> info.standardDataSelector.readingTypes.stream().map(r -> r.mRID).noneMatch(m -> t.getMRID().equals(m)))
                .forEach(selector::removeReadingType);
        info.standardDataSelector.readingTypes.stream()
                .map(r -> r.mRID)
                .filter(m -> selector.getReadingTypes().stream().map(ReadingType::getMRID).noneMatch(s -> s.equals(m)))
                .forEach(selector::addReadingType);
    }

    private void updateProperties(DataExportTaskInfo info, ExportTask task) {
        List<PropertySpec> propertiesSpecs = dataExportService.getPropertiesSpecsForProcessor(info.dataProcessor.name);
        propertiesSpecs.stream()
                .forEach(spec -> {
                    Object value = propertyUtils.findPropertyValue(spec, info.properties);
                    task.setProperty(spec.getName(), value);
                });
    }

    private ScheduleExpression getScheduleExpression(DataExportTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
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

    private ExportTask fetchDataExportTask(long id) {
        return dataExportService.findExportTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private DataExportOccurrence fetchDataExportOccurrence(long id, ExportTask task) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
