package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
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
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.streams.Functions;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/dataexporttask")
public class DataExportTaskResource {

    static final String X_CONNEXO_APPLICATION_NAME = "X-CONNEXO-APPLICATION-NAME";

    private final DataExportService dataExportService;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final PropertyValueInfoService propertyValueInfoService;
    private final DataSourceInfoFactory dataSourceInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DataExportTaskInfoFactory dataExportTaskInfoFactory;
    private final DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory;

    @Inject
    public DataExportTaskResource(DataExportService dataExportService, TimeService timeService, MeteringGroupsService meteringGroupsService,
                                  MeteringService meteringService, Thesaurus thesaurus, PropertyValueInfoService propertyValueInfoService,
                                  ConcurrentModificationExceptionFactory conflictFactory, DataSourceInfoFactory dataSourceInfoFactory,
                                  DataExportTaskInfoFactory dataExportTaskInfoFactory, DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory) {
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
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


    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    public PagedInfoList getAllDataExportTaskHistory(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        String applicationName = getApplicationNameFromCode(appCode);
        ExportTaskFinder finder = dataExportService.findExportTasks().ofApplication(applicationName);
        queryParameters.getStart().ifPresent(finder::setStart);
        queryParameters.getLimit().ifPresent(finder::setLimit);

        List<DataExportTaskHistoryInfo> infos = finder.stream()
                .flatMap(task -> getAllHistoryFromTask(task, filter).stream()).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
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
    @RolesAllowed({Privileges.Constants.RUN_DATA_EXPORT_TASK})
    @Transactional
    public Response triggerDataExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
        info.id = id;
        ExportTask exportTask = dataExportService.findAndLockExportTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> dataExportService.findExportTask(info.id)
                                .map(ExportTask::getVersion)
                                .orElse(null))
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, info.name)
                        .supplier());
        exportTask.triggerNow();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK)
    @Transactional
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
            propertiesSpecsForDataSelector.forEach(spec -> {
                Object value = propertyValueInfoService.findPropertyValue(spec, info.dataSelector.properties);
                builder.addProperty(spec.getName()).withValue(value);
            });
        } else {
            switch (info.dataSelector.selectorType) {
                case DEFAULT_READINGS: {
                    if (info.standardDataSelector.exportUpdate && info.standardDataSelector.exportAdjacentData && info.standardDataSelector.updateWindow.id == null) {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateTimeFrame");
                    }
                    if (info.standardDataSelector.exportUpdate && info.standardDataSelector.updatePeriod.id == null) {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "updateWindow");
                    }
                    if (info.destinations.isEmpty()) {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "destinationsFieldcontainer");
                    }
                    DataExportTaskBuilder.MeterReadingSelectorBuilder selectorBuilder = builder.selectingMeterReadings()
                            .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                            .fromUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                            .withUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow))
                            .withValidatedDataOption(info.standardDataSelector.validatedDataOption)
                            .fromEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id))
                            .continuousData(info.standardDataSelector.exportContinuousData)
                            .exportComplete(info.standardDataSelector.exportComplete)
                            .exportUpdate(info.standardDataSelector.exportUpdate);
                    info.standardDataSelector.readingTypes.stream()
                            .map(r -> meteringService.getReadingType(r.mRID))
                            .flatMap(Functions.asStream())
                            .forEach(selectorBuilder::fromReadingType);
                    selectorBuilder.endSelection();
                    break;
                }
                case DEFAULT_USAGE_POINT_READINGS: {
                    DataExportTaskBuilder.UsagePointReadingSelectorBuilder selectorBuilder = builder.selectingUsagePointReadings()
                            .fromUsagePointGroup(usagePointGroup(info.standardDataSelector.usagePointGroup.id))
                            .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                            .continuousData(info.standardDataSelector.exportContinuousData)
                            .exportComplete(info.standardDataSelector.exportComplete)
                            .withValidatedDataOption(info.standardDataSelector.validatedDataOption);
                    info.standardDataSelector.readingTypes.stream()
                            .map(r -> meteringService.getReadingType(r.mRID))
                            .flatMap(Functions.asStream())
                            .forEach(selectorBuilder::fromReadingType);
                    selectorBuilder.endSelection();
                    break;
                }
                case DEFAULT_EVENTS: {
                    DataExportTaskBuilder.EventSelectorBuilder selectorBuilder = builder.selectingEventTypes()
                            .fromExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                            .fromEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id));
                    info.standardDataSelector.eventTypeCodes.stream()
                            .map(r -> r.eventFilterCode)
                            .forEach(selectorBuilder::fromEventType);
                    selectorBuilder.endSelection();
                    break;
                }
            }
        }

        List<PropertySpec> propertiesSpecsForProcessor = dataExportService.getPropertiesSpecsForFormatter(info.dataProcessor.name);

        propertiesSpecsForProcessor.forEach(spec -> {
            Object value = propertyValueInfoService.findPropertyValue(spec, info.dataProcessor.properties);
            builder.addProperty(spec.getName()).withValue(value);
        });

        ExportTask dataExportTask = builder.create();
        info.destinations.forEach(destinationInfo -> destinationInfo.type.create(dataExportTask, destinationInfo));
        return Response.status(Response.Status.CREATED)
                .entity(dataExportTaskInfoFactory.asInfo(dataExportTask))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK)
    @Transactional
    public Response removeDataExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
        String taskName = info.name;
        try {
            info.id = id;
            ExportTask task = findAndLockExportTask(info);
            if (!task.canBeDeleted()) {
                throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_STATUS_BUSY, "status");
            }
            taskName = task.getName();
            task.delete();
            return Response.status(Response.Status.OK).build();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", taskName);
        }
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK})
    @Transactional
    public Response updateExportTask(@PathParam("id") long id, DataExportTaskInfo info) {
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
            task.getStandardDataSelectorConfig()
                    .orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST))
                    .apply(new StandardDataSelectorUpdater(selectorType, info));
        }

        updateProperties(info, task);
        updateDestinations(info, task);
        task.update();
        return Response.status(Response.Status.OK).entity(dataExportTaskInfoFactory.asInfo(task)).build();
    }

    private class StandardDataSelectorUpdater implements DataSelectorConfig.DataSelectorConfigVisitor {

        private final SelectorType selectorType;
        private final DataExportTaskInfo info;

        StandardDataSelectorUpdater(SelectorType selectorType, DataExportTaskInfo info) {
            this.selectorType = selectorType;
            this.info = info;
        }

        @Override
        public void visit(MeterReadingSelectorConfig config) {
            if (!selectorType.equals(SelectorType.DEFAULT_READINGS)) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            MeterReadingSelectorConfig.Updater updater = config.startUpdate()
                    .setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                    .setExportUpdate(info.standardDataSelector.exportUpdate)
                    .setUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                    .setUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow))
                    .setEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id))
                    .setExportOnlyIfComplete(info.standardDataSelector.exportComplete)
                    .setValidatedDataOption(info.standardDataSelector.validatedDataOption)
                    .setExportContinuousData(info.standardDataSelector.exportContinuousData);
            updateReadingTypes(config, updater, info);
        }

        @Override
        public void visit(UsagePointReadingSelectorConfig config) {
            if (!selectorType.equals(SelectorType.DEFAULT_USAGE_POINT_READINGS)) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            UsagePointReadingSelectorConfig.Updater updater = config.startUpdate()
                    .setUsagePointGroup(usagePointGroup(info.standardDataSelector.usagePointGroup.id))
                    .setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod))
                    .setExportContinuousData(info.standardDataSelector.exportContinuousData)
                    .setExportOnlyIfComplete(info.standardDataSelector.exportComplete)
                    .setValidatedDataOption(info.standardDataSelector.validatedDataOption);
            updateReadingTypes(config, updater, info);
        }

        @Override
        public void visit(EventSelectorConfig config) {
            if (!selectorType.equals(SelectorType.DEFAULT_EVENTS)) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            EventSelectorConfig.Updater updater = config.startUpdate()
                    .setEndDeviceGroup(endDeviceGroup(info.standardDataSelector.deviceGroup.id))
                    .setExportPeriod(getRelativePeriod(info.standardDataSelector.exportPeriod));
            updateEvents(config, updater, info);
            updater.complete();
        }
    }

    private void updateReadingTypes(ReadingDataSelectorConfig selectorConfig, ReadingDataSelectorConfig.Updater updater, DataExportTaskInfo exportTaskInfo) {
        // process removed reading types
        selectorConfig.getReadingTypes().stream()
                .filter(readingType -> exportTaskInfo.standardDataSelector.readingTypes.stream()
                        .map(info -> info.mRID)
                        .noneMatch(readingType::equals))
                .forEach(updater::removeReadingType);

        // process added reading types
        exportTaskInfo.standardDataSelector.readingTypes.stream()
                .map(info -> info.mRID)
                .filter(mRID -> selectorConfig.getReadingTypes()
                        .stream()
                        .map(ReadingType::getMRID)
                        .noneMatch(mRID::equals))
                .map(meteringService::getReadingType)
                .flatMap(Functions.asStream())
                .forEach(updater::addReadingType);
    }

    private void updateEvents(EventSelectorConfig selectorConfig, EventSelectorConfig.Updater updater, DataExportTaskInfo info) {
        // process removed event types
        selectorConfig.getEventTypeFilters().stream()
                .filter(t -> info.standardDataSelector.eventTypeCodes.stream()
                        .map(r -> r.eventFilterCode)
                        .noneMatch(m -> t.getCode().equals(m)))
                .map(EndDeviceEventTypeFilter::getCode)
                .forEach(updater::removeEventTypeFilter);

        // process added event types
        info.standardDataSelector.eventTypeCodes.stream()
                .map(r -> r.eventFilterCode)
                .filter(m -> selectorConfig.getEventTypeFilters()
                        .stream()
                        .map(EndDeviceEventTypeFilter::getCode)
                        .noneMatch(m::equals))
                .forEach(updater::addEventTypeFilter);
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
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
        if (filter.hasProperty("status")) {
            occurrencesFinder.withExportStatus(filter.getStringList("status")
                    .stream()
                    .map(DataExportStatus::valueOf)
                    .collect(Collectors.toList()));
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
        List<DataSourceInfo> infos = new ArrayList<>();
        task.getStandardDataSelectorConfig().ifPresent(selectorConfig -> selectorConfig.apply(
                new DataSelectorConfig.DataSelectorConfigVisitor() {
                    @Override
                    public void visit(MeterReadingSelectorConfig config) {
                        infos.addAll(getDataSources(config));
                    }

                    @Override
                    public void visit(UsagePointReadingSelectorConfig config) {
                        infos.addAll(getDataSources(config));
                    }

                    @Override
                    public void visit(EventSelectorConfig config) {
                        // no data sources
                    }

                    private List<DataSourceInfo> getDataSources(ReadingDataSelectorConfig config) {
                        return fetchDataSources(config, queryParameters).stream()
                                .map(dataSourceInfoFactory::asInfo)
                                .collect(Collectors.toList());
                    }
                }
        ));
        return PagedInfoList.fromPagedList("dataSources", infos, queryParameters);
    }

    private List<? extends ReadingTypeDataExportItem> fetchDataSources(ReadingDataSelectorConfig readingDataSelectorConfig, JsonQueryParameters queryParameters) {
        List<ReadingTypeDataExportItem> activeExportItems = readingDataSelectorConfig.getExportItems().stream()
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

    @GET
    @Path("/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DataExportOccurrenceLogInfos getDataExportLogByOccurrence(@PathParam("occurrenceId") long occurrenceId, @BeanParam JsonQueryParameters queryParameters) {
        Optional<DataExportOccurrence> foundOccurrence = dataExportService.findDataExportOccurrence(occurrenceId);

        if (foundOccurrence.isPresent()) {
            DataExportOccurrence dataExportOccurrence = foundOccurrence.get();
            LogEntryFinder finder = dataExportOccurrence.getLogsFinder()
                    .setStart(queryParameters.getStart().orElse(0))
                    .setLimit(queryParameters.getLimit().orElse(0) + 1);
            List<? extends LogEntry> occurrences = finder.find();

            return new DataExportOccurrenceLogInfos(occurrences, thesaurus);
        }

        return new DataExportOccurrenceLogInfos();
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
                        .withActualVersion(() -> dataExportService.findExportTask(info.id)
                                .map(ExportTask::getVersion)
                                .orElse(null))
                        .supplier());
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
                .filter(destination -> info.destinations.stream()
                        .noneMatch(destinationInfo -> destinationInfo.id == destination.getId()))
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

    private List<DataExportTaskHistoryInfo> getAllHistoryFromTask(ExportTask task, JsonQueryFilter filter) {
        return getFinderWithStatusFilter(task.getOccurrencesFinder(), filter)
                .stream()
                .map(occurrence -> dataExportTaskHistoryInfoFactory.asInfo(task.getHistory(), occurrence))
                .collect(Collectors.toList());
    }

    private DataExportOccurrenceFinder getFinderWithStatusFilter(DataExportOccurrenceFinder occurrenceFinder, JsonQueryFilter filter) {
        if (filter.hasProperty("status")) {
            occurrenceFinder.withExportStatus(filter.getStringList("status")
                    .stream()
                    .map(DataExportStatus::valueOf)
                    .collect(Collectors.toList()));
        }

        return occurrenceFinder;
    }

    private Predicate<DestinationInfo> isNewDestination() {
        return destinationInfo -> destinationInfo.id == 0;
    }

    private ScheduleExpression getScheduleExpression(DataExportTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private EndDeviceGroup endDeviceGroup(Object endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(((Number) endDeviceGroupId).longValue()).orElse(null);
    }

    private UsagePointGroup usagePointGroup(Object usagePointGroupId) {
        return meteringGroupsService.findUsagePointGroup(((Number) usagePointGroupId).longValue()).orElse(null);
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
