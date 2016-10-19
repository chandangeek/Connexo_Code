package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventDataSelector;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
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
import javax.ws.rs.HeaderParam;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/dataexporttask")
public class DataExportTaskResource {

    static final String X_CONNEXO_APPLICATION_NAME = "X-CONNEXO-APPLICATION-NAME";

    private final DataExportService dataExportService;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final DataSourceInfoFactory dataSourceInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DataExportTaskInfoFactory dataExportTaskInfoFactory;
    private final DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory;

    @Inject
    public DataExportTaskResource(DataExportService dataExportService, TimeService timeService, MeteringGroupsService meteringGroupsService,
                                  Thesaurus thesaurus, TransactionService transactionService, PropertyValueInfoService propertyValueInfoService,
                                  ConcurrentModificationExceptionFactory conflictFactory, DataSourceInfoFactory dataSourceInfoFactory,
                                  DataExportTaskInfoFactory dataExportTaskInfoFactory, DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory) {
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
        this.dataSourceInfoFactory = dataSourceInfoFactory;
        this.dataExportTaskInfoFactory = dataExportTaskInfoFactory;
        this.dataExportTaskHistoryInfoFactory = dataExportTaskHistoryInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getDataExportTasks(@BeanParam JsonQueryParameters queryParameters, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        String applicationName = getApplicationNameFromCode(appCode);
        ExportTaskFinder finder = dataExportService.findExportTasks().ofApplication(applicationName);
        queryParameters.getStart().ifPresent(finder::setStart);
        queryParameters.getLimit().ifPresent(finder::setLimit);
        List<DataExportTaskInfo> infos = finder.stream()
                .map(dataExportTaskInfoFactory::asInfoWithMinimalHistory)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("dataExportTasks", infos, queryParameters);
    }

    private String getApplicationNameFromCode(String appCode) {
        String applicationName;
        if ("MDC".equals(appCode)) {
            applicationName = "MultiSense";
        } else if ("INS".equals(appCode)) {
            applicationName = "Insight";
        } else {
            applicationName = appCode;
        }
        return applicationName;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        return dataExportTaskInfoFactory.asInfo(findTaskOrThrowException(id, appCode));
    }

    @PUT
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public Response triggerDataExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
        info.id = id;
        transactionService.execute(VoidTransaction.of(() -> dataExportService.findAndLockExportTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> dataExportService.findExportTask(info.id).map(ExportTask::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, info.name)
                        .supplier()).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response addExportTask(DataExportTaskInfo info, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(info.name)
                .setApplication(getApplicationNameFromCode(appCode))
                .setDataFormatterFactoryName(info.dataProcessor.name)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun);

        if (info.standardDataSelector == null) {
            builder.selectingCustom(info.dataSelector.name).endSelection();
            List<PropertySpec> propertiesSpecsForDataSelector = dataExportService.getPropertiesSpecsForDataSelector(info.dataSelector.name);

            propertiesSpecsForDataSelector
                    .forEach(spec -> {
                        Object value = propertyValueInfoService.findPropertyValue(spec, info.dataSelector.properties);
                        builder.addProperty(spec.getName()).withValue(value);
                    });
        } else {
            if (info.dataSelector.selectorType == SelectorType.DEFAULT_READINGS) {
                if (info.standardDataSelector.exportUpdate && info.standardDataSelector.exportAdjacentData && info.standardDataSelector.updateWindow.id == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateTimeFrame");
                }
                if (info.standardDataSelector.exportUpdate && info.standardDataSelector.updatePeriod.id == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateWindow");
                }
                if (info.destinations.isEmpty()) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "destinationsFieldcontainer");
                }

                DataExportTaskBuilder.ReadingTypeSelectorBuilder selectorBuilder = builder.selectingReadingTypes()
                        .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                        .fromUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                        .withUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow))
                        .withValidatedDataOption(info.standardDataSelector.validatedDataOption)
                        .fromEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id))
                        .continuousData(info.standardDataSelector.exportContinuousData)
                        .exportComplete(info.standardDataSelector.exportComplete)
                        .exportUpdate(info.standardDataSelector.exportUpdate);
                info.standardDataSelector.readingTypes.stream()
                        .map(r -> r.mRID)
                        .forEach(selectorBuilder::fromReadingType);
                selectorBuilder.endSelection();
            } else if (info.dataSelector.selectorType == SelectorType.DEFAULT_EVENTS) {
                DataExportTaskBuilder.EventSelectorBuilder selectorBuilder = builder.selectingEventTypes()
                        .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                        .fromEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id));
                info.standardDataSelector.eventTypeCodes.stream()
                        .map(r -> r.eventFilterCode)
                        .forEach(selectorBuilder::fromEventType);
                selectorBuilder.endSelection();
            }
        }

        List<PropertySpec> propertiesSpecsForProcessor = dataExportService.getPropertiesSpecsForFormatter(info.dataProcessor.name);

        propertiesSpecsForProcessor
                .forEach(spec -> {
                    Object value = propertyValueInfoService.findPropertyValue(spec, info.dataProcessor.properties);
                    builder.addProperty(spec.getName()).withValue(value);
                });

        ExportTask dataExportTask;
        try (TransactionContext context = transactionService.getContext()) {
            dataExportTask = builder.create();
            ExportTask exportTask = dataExportTask;
            info.destinations
                    .forEach(destinationInfo -> destinationInfo.type.create(exportTask, destinationInfo));
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(dataExportTaskInfoFactory.asInfo(dataExportTask)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK)
    public Response removeDataExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
        String taskName = info.name;
        try (TransactionContext context = transactionService.getContext()) {
            info.id = id;
            ExportTask task = findAndLockExportTask(info);

            if (!task.canBeDeleted()) {
                throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_STATUS_BUSY, "status");
            }
            taskName = task.getName();
            task.delete();
            context.commit();
            return Response.status(Response.Status.OK).build();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", taskName);
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK})
    public Response updateExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
        try (TransactionContext context = transactionService.getContext()) {
            info.id = id;
            ExportTask task = findAndLockExportTask(info);

            task.setName(info.name);
            task.setScheduleExpression(getScheduleExpression(info));
            task.setNextExecution(info.nextRun);

            if (info.standardDataSelector != null) {
                if (info.standardDataSelector.exportUpdate && info.standardDataSelector.exportAdjacentData && info.standardDataSelector.updateWindow.id == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateTimeFrame");
                }
                if (info.standardDataSelector.exportUpdate && info.standardDataSelector.updatePeriod.id == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateWindow");
                }
                if (info.destinations.isEmpty()) {
                    throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "destinationsFieldcontainer");
                }
                String selectorString = task.getDataSelectorFactory().getName();
                SelectorType selectorType = SelectorType.forSelector(selectorString);
                if (selectorType.equals(SelectorType.DEFAULT_READINGS)) {
                    StandardDataSelector selector = task.getReadingTypeDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
                    selector.setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod));
                    selector.setExportUpdate(info.standardDataSelector.exportUpdate);
                    selector.setUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod));
                    selector.setUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow));
                    selector.setEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id));
                    selector.setExportOnlyIfComplete(info.standardDataSelector.exportComplete);
                    selector.setValidatedDataOption(info.standardDataSelector.validatedDataOption);
                    selector.setExportContinuousData(info.standardDataSelector.exportContinuousData);
                    selector.save();
                    updateReadingTypes(info, task);
                } else if (selectorType.equals(SelectorType.DEFAULT_EVENTS)) {
                    EventDataSelector selector = task.getEventDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
                    selector.setEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id));
                    selector.setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod));
                    updateEvents(info, task);
                }
            }

            updateProperties(info, task);
            updateDestinations(info, task);

            task.update();
            context.commit();
            return Response.status(Response.Status.CREATED).entity(dataExportTaskInfoFactory.asInfo(task)).build();
        }
    }

    private void updateEvents(DataExportTaskInfo info, ExportTask task) {
        EventDataSelector selector = task.getEventDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        Set<String> toRemove = selector.getEventTypeFilters().stream()
                .filter(t -> info.standardDataSelector.eventTypeCodes
                        .stream()
                        .map(r -> r.eventFilterCode)
                        .noneMatch(m -> t.getCode().equals(m)))
                .map(EndDeviceEventTypeFilter::getCode)
                .collect(Collectors.toSet());
        toRemove.forEach(selector::removeEventTypeFilter);
        info.standardDataSelector.eventTypeCodes.stream()
                .map(r -> r.eventFilterCode)
                .filter(m -> selector.getEventTypeFilters()
                        .stream()
                        .map(EndDeviceEventTypeFilter::getCode)
                        .noneMatch(s -> s.equals(m)))
                .forEach(selector::addEventTypeFilter);
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getDataExportTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                  @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        ExportTask task = findTaskOrThrowException(id, appCode);
        DataExportOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStart().orElse(0))
                .setLimit(queryParameters.getLimit().orElse(0) + 1);

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

        History<ExportTask> history = task.getHistory();
        List<DataExportTaskHistoryInfo> infos = occurrencesFinder.stream()
                .map(occurrence -> dataExportTaskHistoryInfoFactory.asInfo(history, occurrence))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @GET
    @Path("/{id}/datasources")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getDataSources(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        ExportTask task = findTaskOrThrowException(id, appCode);
        List<DataSourceInfo> infos = task.getReadingTypeDataSelector()
                .map(readingTypeDataSelector -> getDataSources(readingTypeDataSelector, queryParameters))
                .orElse(Collections.emptyList())
                .stream()
                .map(dataSourceInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("dataSources", infos, queryParameters);
    }

    private List<? extends ReadingTypeDataExportItem> getDataSources(StandardDataSelector standardDataSelector, JsonQueryParameters queryParameters) {
        List<ReadingTypeDataExportItem> activeExportItems = standardDataSelector.getExportItems().stream()
                .filter(ReadingTypeDataExportItem::isActive)
                .filter(item -> item.getLastRun().isPresent())
                .collect(Collectors.toList());
        return ListPager.of(activeExportItems).from(queryParameters).find();
    }

    @GET
    @Path("/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DataExportOccurrenceLogInfos getDataExportTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                                 @Context SecurityContext securityContext, @Context UriInfo uriInfo, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        ExportTask task = findTaskOrThrowException(id, appCode);
        DataExportOccurrence occurrence = fetchDataExportOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        DataExportOccurrenceLogInfos infos = new DataExportOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    private ExportTask findTaskOrThrowException(long id, String appCode) {
        String application = getApplicationNameFromCode(appCode);
        return dataExportService.findExportTask(id)
                .filter(exportTask -> application.equals(exportTask.getApplication()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private ExportTask findAndLockExportTask(DataExportTaskInfo info) {
        return dataExportService.findAndLockExportTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> dataExportService.findExportTask(info.id).map(ExportTask::getVersion).orElse(null))
                        .supplier());
    }

    private void updateReadingTypes(DataExportTaskInfo info, ExportTask task) {
        StandardDataSelector selector = task.getReadingTypeDataSelector().orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        selector.getReadingTypes().stream()
                .filter(t -> info.standardDataSelector.readingTypes.stream().map(r -> r.mRID).noneMatch(m -> t.getMRID().equals(m)))
                .forEach(selector::removeReadingType);
        info.standardDataSelector.readingTypes.stream()
                .map(r -> r.mRID)
                .filter(m -> selector.getReadingTypes().stream().map(ReadingType::getMRID).noneMatch(s -> s.equals(m)))
                .forEach(selector::addReadingType);
    }

    private void updateProperties(DataExportTaskInfo info, ExportTask task) {
        List<PropertySpec> propertiesSpecsForDataProcessor = dataExportService.getPropertiesSpecsForFormatter(info.dataProcessor.name);
        List<PropertySpec> propertiesSpecsOfCurrentTask = task.getDataFormatterFactory().getPropertySpecs();
        propertiesSpecsOfCurrentTask.forEach(task::removeProperty);

        task.setDataFormatterFactoryName(info.dataProcessor.name);
        propertiesSpecsForDataProcessor
                .forEach(spec -> {
                    Object value = propertyValueInfoService.findPropertyValue(spec, info.dataProcessor.properties);
                    task.setProperty(spec.getName(), value);
                });
        if (info.dataSelector.selectorType == SelectorType.CUSTOM) {
            List<PropertySpec> propertiesSpecsForDataSelector = dataExportService.getPropertiesSpecsForDataSelector(info.dataSelector.name);
            propertiesSpecsForDataSelector
                    .forEach(spec -> {
                        Object value = propertyValueInfoService.findPropertyValue(spec, info.dataSelector.properties);
                        task.setProperty(spec.getName(), value);
                    });
        }
    }

    private void updateDestinations(DataExportTaskInfo info, ExportTask task) {
        // remove the ones no longer in the info
        task.getDestinations().stream()
                .filter(destination -> info.destinations.stream().noneMatch(destinationInfo -> destinationInfo.id == destination.getId()))
                .collect(Collectors.toList())
                .forEach(task::removeDestination);
        // create the new ones
        info.destinations.stream()
                .filter(isNewDestination())
                .forEach(destinationInfo -> destinationInfo.type.create(task, destinationInfo));
        // update the ones that stay
        info.destinations.stream()
                .filter(isNewDestination().negate())
                .forEach(destinationInfo -> task.getDestinations().stream()
                        .filter(destination -> destination.getId() == destinationInfo.id)
                        .findAny()
                        .ifPresent(destination -> destinationInfo.type.update(destination, destinationInfo)));
    }

    private Predicate<DestinationInfo> isNewDestination() {
        return destinationInfo -> destinationInfo.id == 0;
    }

    private ScheduleExpression getScheduleExpression(DataExportTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }


    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElse(null);
    }

    private RelativePeriod getRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        if ((relativePeriodInfo == null) || (relativePeriodInfo.id == null)) {
            return null;
        }
        return timeService.findRelativePeriod(relativePeriodInfo.id).orElse(null);
    }

    private DataExportOccurrence fetchDataExportOccurrence(long id, ExportTask task) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
