/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.SelectorType;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.collect.Range;
import org.glassfish.hk2.api.ServiceLocator;

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
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/dataexporttask")
public class DataExportTaskResource {

    static final String X_CONNEXO_APPLICATION_NAME = "X-CONNEXO-APPLICATION-NAME";

    private final DataExportService dataExportService;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Thesaurus thesaurus;
    private final PropertyValueInfoService propertyValueInfoService;
    private final DataSourceInfoFactory dataSourceInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DataExportTaskInfoFactory dataExportTaskInfoFactory;
    private final DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory;
    private final Clock clock;
    private final ServiceLocator serviceLocator;

    @Inject
    public DataExportTaskResource(DataExportService dataExportService, TimeService timeService, MeteringGroupsService meteringGroupsService,
                                  MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService, Thesaurus thesaurus,
                                  PropertyValueInfoService propertyValueInfoService, ConcurrentModificationExceptionFactory conflictFactory,
                                  DataSourceInfoFactory dataSourceInfoFactory, DataExportTaskInfoFactory dataExportTaskInfoFactory,
                                  DataExportTaskHistoryInfoFactory dataExportTaskHistoryInfoFactory, Clock clock, ServiceLocator serviceLocator) {
        this.dataExportService = dataExportService;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.thesaurus = thesaurus;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;
        this.dataSourceInfoFactory = dataSourceInfoFactory;
        this.dataExportTaskInfoFactory = dataExportTaskInfoFactory;
        this.dataExportTaskHistoryInfoFactory = dataExportTaskHistoryInfoFactory;
        this.clock = clock;
        this.serviceLocator = serviceLocator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    public PagedInfoList getDataExportTasks(@BeanParam JsonQueryParameters queryParameters, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        ExportTaskFinder finder = dataExportService.findExportTasks().ofApplication(getApplicationNameFromCode(appCode));
        queryParameters.getStart().ifPresent(finder::setStart);
        queryParameters.getLimit().map(limit -> limit + 1).ifPresent(finder::setLimit);
        List<DataExportTaskInfo> infos = finder.stream()
                .map(dataExportTaskInfoFactory::asInfoWithMinimalHistory)
                .sorted((dt1, dt2) -> dt1.name.compareToIgnoreCase(dt2.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("dataExportTasks", infos, queryParameters);
    }

    @GET
    @Path("/usagepoint/{usagePointId}/{purposeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    public PagedInfoList getDataExportTasksOnUsagePointAndPurpose(@BeanParam JsonQueryParameters queryParameters, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode, @PathParam("usagePointId") String usagePointId, @PathParam("purposeId") long purposeId) {
        String applicationName = getApplicationNameFromCode(appCode);


        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(usagePointId);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint.get());
        MetrologyContract metrologyContract = findMetrologyContractOrThrowException(effectiveMetrologyConfigurationOnUsagePoint, purposeId);

        Collection<Set<ReadingType>> readingTypeInfos = metrologyContract.sortReadingTypesByDependencyLevel();

        ExportTaskFinder finder = dataExportService.findExportTasks().ofApplication(applicationName);
        List<DataExportTaskInfo> infos = finder.stream()
                .map(dataExportTaskInfoFactory::asInfoWithMinimalHistory)
                .collect(Collectors.toList());
        List<DataExportTaskInfo> filteredTasks = new ArrayList<>();
        for (DataExportTaskInfo dataExportTaskInfo : infos) {
            Optional<UsagePointGroup> group = meteringGroupsService.findUsagePointGroup(((Number) dataExportTaskInfo.standardDataSelector.usagePointGroup.id).longValue());

            if ((group.isPresent() && isMember(usagePoint.get(), group.get())) &&
                    (dataExportTaskInfo.standardDataSelector.purpose == null || (dataExportTaskInfo.standardDataSelector.purpose != null && metrologyContract.getMetrologyPurpose()
                            .getName()
                            .equals(dataExportTaskInfo.standardDataSelector.purpose.name))) &&
                    containsAtLeastOneReadingType(readingTypeInfos, dataExportTaskInfo.standardDataSelector.readingTypes)) {
                filteredTasks.add(dataExportTaskInfo);
            }
        }

        return PagedInfoList.fromPagedList("dataExportTasks", filteredTasks, queryParameters);
    }

    private boolean containsAtLeastOneReadingType(Collection<Set<ReadingType>> readingTypesFromUsagePoint, List<ReadingTypeInfo> readingTypesFromExportTask) {
        for (ReadingTypeInfo rtFromExportTask : readingTypesFromExportTask) {
            Iterator iter = readingTypesFromUsagePoint.iterator();
            while (iter.hasNext()) {
                for (Object readingType : (HashSet) iter.next()) {
                    if (((ReadingType) readingType).getMRID().equals(rtFromExportTask.mRID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isMember(UsagePoint usagePoint, UsagePointGroup usagePointGroup) {
        return !meteringService.getUsagePointQuery()
                .select(Where.where("id").isEqualTo(usagePoint.getId())
                        .and(ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "id")), 1, 1)
                .isEmpty();
    }

    public EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfigurationByUsagePointOrThrowException(UsagePoint usagePoint) {
        return usagePoint.getCurrentEffectiveMetrologyConfiguration().orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, long contractId) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getId() == contractId).findAny().orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    @Transactional
    public PagedInfoList getAllDataExportTaskHistory(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        String applicationName = getApplicationNameFromCode(appCode);
        DataExportOccurrenceFinder occurrencesFinder = dataExportService.getDataExportOccurrenceFinder();
        List<Long> taskIds = dataExportService.findExportTasks().ofApplication(applicationName)
                .stream()
                .map(ExportTask::getId)
                .collect(Collectors.toList());
        occurrencesFinder.setStart(queryParameters.getStart().orElse(0));
        occurrencesFinder.setLimit(queryParameters.getLimit().orElse(0) + 1);
        occurrencesFinder.setOrder(queryParameters.getSortingColumns());

        return PagedInfoList.fromPagedList("data",
                getHistoryFromTasks(filter, occurrencesFinder, taskIds)
                        .find()
                        .stream()
                        .map(occurrence -> dataExportTaskHistoryInfoFactory.asInfo(occurrence.getTask().getHistory(), occurrence))
                        .collect(Collectors.toList()),
                queryParameters);
    }

    @GET
    @Path("/history/count")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    @Transactional
    public Response getAllDataExportTaskHistoryCount(@BeanParam JsonQueryFilter filter, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        String applicationName = getApplicationNameFromCode(appCode);
        DataExportOccurrenceFinder occurrencesFinder = dataExportService.getDataExportOccurrenceFinder();
        List<Long> taskIds = dataExportService.findExportTasks().ofApplication(applicationName)
                .stream()
                .map(ExportTask::getId)
                .collect(Collectors.toList());

        return Response.ok(getHistoryFromTasks(filter, occurrencesFinder, taskIds).stream().count()).build();
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
    @Transactional
    public DataExportTaskInfo getDataExportTask(@PathParam("id") long id, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        return dataExportTaskInfoFactory.asInfo(findTaskOrThrowException(id, appCode));
    }

    @GET
    @Path("/recurrenttask/{recurrenttaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public DataExportTaskInfo getDataExportTaskByRecurrentTaskId(@PathParam("recurrenttaskId") long id, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        return dataExportTaskInfoFactory.asInfo(findTaskByRecurrentTaskIdOrThrowException(id, appCode));
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

    @PUT
    @Transactional
    @Path("/{id}/triggerWithParams")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public Response triggerWithParametersDataExportTask(@PathParam("id") long id, DataExportTaskRunInfo runInfo) {
        runInfo.task.id = id;
        ExportTask exportTask = dataExportService.findAndLockExportTask(runInfo.task.id, runInfo.task.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(runInfo.task.name)
                        .withActualVersion(() -> dataExportService.findExportTask(runInfo.task.id)
                                .map(ExportTask::getVersion)
                                .orElse(null))
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, runInfo.task.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, runInfo.task.name)
                        .supplier());

        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        validationBuilder.notEmpty(runInfo.startOn, "startOn");
        validateExportWindow(validationBuilder, runInfo, exportTask);
        validateUpdateData(validationBuilder, runInfo, exportTask);
        validationBuilder.validate();

        Instant triggerTime = clock.instant();
        exportTask.addExportRunParameters(triggerTime, runInfo.exportWindowStart, runInfo.exportWindowEnd, runInfo.updateDataStart, runInfo.updateDataEnd);
        exportTask.triggerAt(runInfo.startOn, triggerTime);
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
                .setLogLevel(info.logLevel)
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
                    if (info.standardDataSelector.exportComplete == null) {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "data-selector-export-complete");
                    }
                    if (info.standardDataSelector.validatedDataOption == null) {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "data-selector-validated-data");
                    }
                    if (info.standardDataSelector.exportComplete.equals(MissingDataOption.EXCLUDE_OBJECT)) {
                        throw new LocalizedFieldValidationException(MessageSeeds.NOT_SUPPORTED_PROPERTY_VALUE, "");
                    }
                    if (info.standardDataSelector.validatedDataOption.equals(ValidatedDataOption.EXCLUDE_OBJECT)) {
                        throw new LocalizedFieldValidationException(MessageSeeds.NOT_SUPPORTED_PROPERTY_VALUE, "");
                    }
                    DataExportTaskBuilder.MeterReadingSelectorBuilder selectorBuilder = builder.selectingMeterReadings(info.dataSelector.name)
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
                            .fromUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                            .withUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow))
                            .exportUpdate(info.standardDataSelector.exportUpdate)
                            .continuousData(info.standardDataSelector.exportContinuousData)
                            .exportComplete(info.standardDataSelector.exportComplete)
                            .withValidatedDataOption(info.standardDataSelector.validatedDataOption);
                    info.standardDataSelector.readingTypes.stream()
                            .map(r -> meteringService.getReadingType(r.mRID))
                            .flatMap(Functions.asStream())
                            .forEach(selectorBuilder::fromReadingType);
                    if (info.standardDataSelector.purpose.id != null) {
                        selectorBuilder = selectorBuilder.fromMetrologyPurpose(metrologyConfigurationService.findMetrologyPurpose(info.standardDataSelector.purpose.id).get());
                    }
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
        // Next line is needed here while this is the mechanism we have: config is saved at db save time (journal) and we need to make sure that
        // next run is after we save them: CONM-676
        // This is ugly but working with what we have ... also I hope that this is included in a transaction as annotated and will be rolled back if this validation fails
        ScheduleValidator.validate(info.nextRun, clock.instant());
        info.destinations.forEach(destinationInfo -> destinationInfo.type.create(serviceLocator, dataExportTask, destinationInfo));
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
        task.setLogLevel(info.logLevel);
        task.setScheduleExpression(getScheduleExpression(info));
        task.setNextExecution(info.nextRun);
        if (info.suspendUntilExport != null) {
            task.setSuspendUntil(info.suspendUntilExport);
        }

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
            SelectorType selectorType = task.getDataSelectorFactory().getSelectorType();
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
                    .setExportUpdate(info.standardDataSelector.exportUpdate)
                    .setUpdatePeriod(getRelativePeriod(info.standardDataSelector.updatePeriod))
                    .setUpdateWindow(getRelativePeriod(info.standardDataSelector.updateWindow))
                    .setExportContinuousData(info.standardDataSelector.exportContinuousData)
                    .setExportOnlyIfComplete(info.standardDataSelector.exportComplete)
                    .setValidatedDataOption(info.standardDataSelector.validatedDataOption);
            if (info.standardDataSelector.purpose.id != null) {
                updater = updater.setMetrologyPurpose(metrologyPurpose(info.standardDataSelector.purpose.id));
            } else {
                updater = updater.setMetrologyPurpose(null);
            }
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
    @Transactional
    public PagedInfoList getDataExportTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                  @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        ExportTask task = findTaskOrThrowException(id, appCode);
        DataExportOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStart().orElse(0))
                .setLimit(queryParameters.getLimit().orElse(0) + 1)
                .setOrder(queryParameters.getSortingColumns());

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
            try {
                occurrencesFinder.withExportStatus(filter.getStringList("status")
                        .stream()
                        .map(DataExportStatus::valueOf)
                        .collect(Collectors.toList()));
            } catch (Exception ex) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "status");
            }
        }
        occurrencesFinder.setOrder(queryParameters.getSortingColumns());
        History<ExportTask> history = task.getHistory();
        List<DataExportTaskHistoryInfo> infos = occurrencesFinder.stream()
                .map(occurrence -> dataExportTaskHistoryInfoFactory.asInfo(history, occurrence))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @PUT
    @Path("history/{historyId}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.RUN_DATA_EXPORT_TASK})
    @Transactional
    public Response triggerDataExportHistoryTask(@PathParam("historyId") long historyId, DataExportTaskHistoryInfo historyInfo) {
        dataExportService.findExportTask(historyInfo.task.id)
                .ifPresent(exportTask ->
                {
                    try(QueryStream<DataExportOccurrence> dataExportOccurrenceStream = exportTask.getOccurrencesFinder()
                                .setId(historyId).stream()) {
                        DataExportOccurrence dataExportOccurrence = dataExportOccurrenceStream
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Export history task was not found."));
                        exportTask.retryNow(dataExportOccurrence);
                    }
                });
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("history/{historyId}/setToFailed")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    @Transactional
    public Response setToFailedDataExportHistoryTask(@PathParam("historyId") long historyId, DataExportTaskHistoryInfo historyInfo) {
        dataExportService.findExportTask(historyInfo.task.id)
                .ifPresent(exportTask ->
                {
                    try (QueryStream<DataExportOccurrence> dataExportOccurenceStream = exportTask.getOccurrencesFinder()
                            .setId(historyId).stream()) {
                        DataExportOccurrence dataExportOccurrence = dataExportOccurenceStream
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Export history task was not found."));

                        dataExportOccurrence.setToFailed();
                    }
                });
        return Response.status(Response.Status.OK).build();
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
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
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
    @Path("/history/{occurrenceId}/logs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    @Transactional
    public PagedInfoList getDataExportLogByOccurrence(@PathParam("occurrenceId") long occurrenceId, @BeanParam JsonQueryParameters queryParameters) {
        LogEntryFinder finder = findDataExportOccurrenceOrThrowException(occurrenceId).getLogsFinder();
        queryParameters.getStart().ifPresent(finder::setStart);
        queryParameters.getLimit().ifPresent(finder::setLimit);
        List<DataExportOccurrenceLogInfo> infos = finder.find()
                .stream()
                .map(DataExportOccurrenceLogInfo::from)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @GET
    @Path("/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK, Privileges.Constants.VIEW_HISTORY})
    @Transactional
    public DataExportTaskHistoryInfo getDataExportOccurrence(@PathParam("occurrenceId") long occurrenceId, @BeanParam JsonQueryParameters queryParameters) {
        return dataExportTaskHistoryInfoFactory.asInfo(findDataExportOccurrenceOrThrowException(occurrenceId));
    }

    private ExportTask findTaskOrThrowException(long id, String appCode) {
        String application = getApplicationNameFromCode(appCode);
        return dataExportService.findExportTask(id)
                .filter(exportTask -> application.equals(exportTask.getApplication()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private ExportTask findTaskByRecurrentTaskIdOrThrowException(long id, String appCode) {
        String application = getApplicationNameFromCode(appCode);
        return dataExportService.findExportTaskByRecurrentTask(id)
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
                .forEach(destinationInfo -> destinationInfo.type.create(serviceLocator, task, destinationInfo));
        // update the ones that stay
        info.destinations.stream()
                .filter(isNewDestination().negate())
                .forEach(destinationInfo -> task.getDestinations().stream()
                        .filter(destination -> destination.getId() == destinationInfo.id)
                        .findAny()
                        .ifPresent(destination -> destinationInfo.type.update(serviceLocator, destination, destinationInfo)));
    }

    private DataExportOccurrenceFinder getHistoryFromTasks(JsonQueryFilter filter, DataExportOccurrenceFinder occurrencesFinder, List<Long> exportTaskIds) {
        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                occurrencesFinder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")));
            } else {
                occurrencesFinder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                occurrencesFinder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")));
            } else {
                occurrencesFinder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withEndDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }
        if (filter.hasProperty("exportTask")) {
            occurrencesFinder.withExportTask(filter.getLongList("exportTask"));
        }
        if (filter.hasProperty("status")) {
            try {
                occurrencesFinder.withExportStatus(filter.getStringList("status")
                        .stream()
                        .map(DataExportStatus::valueOf)
                        .collect(Collectors.toList()));
            } catch (Exception ex) {
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "status");
            }
        }
        return occurrencesFinder
                .withExportTask(exportTaskIds);
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

    private MetrologyPurpose metrologyPurpose(Object purposeId) {
        return metrologyConfigurationService.findMetrologyPurpose(((Number) purposeId).longValue()).orElse(null);
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

    private DataExportOccurrence findDataExportOccurrenceOrThrowException(long occurrenceId) {
        return dataExportService.findDataExportOccurrence(occurrenceId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void validateExportWindow(RestValidationBuilder validationBuilder, DataExportTaskRunInfo runInfo, ExportTask exportTask) {
        Optional<DataSelectorConfig> selector = exportTask.getStandardDataSelectorConfig();
        validationBuilder.notEmpty(runInfo.exportWindowEnd, "exportWindowEnd");
        if (selector.isPresent() && !selector.get().isExportContinuousData()) {
            validationBuilder.notEmpty(runInfo.exportWindowStart, "exportWindowStart");
            if ((runInfo.exportWindowStart != null) && (runInfo.exportWindowEnd != null) && runInfo.exportWindowStart.isAfter(runInfo.exportWindowEnd)) {
                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.END_DATE_MUST_BE_GREATER_THAN_START_DATE, "exportWindowEnd"));
            }
        }
    }

    private void validateUpdateData(RestValidationBuilder validationBuilder, DataExportTaskRunInfo runInfo, ExportTask exportTask) {
        exportTask.getStandardDataSelectorConfig().ifPresent(selectorConfig -> selectorConfig.apply(
                new DataSelectorConfig.DataSelectorConfigVisitor() {
                    @Override
                    public void visit(MeterReadingSelectorConfig config) {
                        if (config.getStrategy().isExportUpdate()) {
                            validationBuilder.notEmpty(runInfo.updateDataStart, "updateDataStart");
                            validationBuilder.notEmpty(runInfo.updateDataEnd, "updateDataEnd");
                            if ((runInfo.updateDataStart != null) && (runInfo.updateDataEnd != null) && runInfo.updateDataStart.isAfter(runInfo.updateDataEnd)) {
                                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.END_DATE_MUST_BE_GREATER_THAN_START_DATE, "updateDataEnd"));
                            }
                        }
                    }

                    @Override
                    public void visit(UsagePointReadingSelectorConfig config) {
                    }

                    @Override
                    public void visit(EventSelectorConfig config) {
                    }
                }
        ));
    }
}
