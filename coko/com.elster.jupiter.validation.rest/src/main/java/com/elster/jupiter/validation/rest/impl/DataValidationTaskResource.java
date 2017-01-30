package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.TaskLogLevel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationOccurrenceLogInfos;
import com.elster.jupiter.validation.rest.DataValidationTaskHistoryInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;
import com.elster.jupiter.validation.security.Privileges;

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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/validationtasks")
public class DataValidationTaskResource {

    private final ValidationService validationService;
    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;

    @Inject
    public DataValidationTaskResource(ValidationService validationService,
                                      TransactionService transactionService,
                                      MeteringGroupsService meteringGroupsService,
                                      MetrologyConfigurationService metrologyConfigurationService,
                                      Thesaurus thesaurus,
                                      ConcurrentModificationExceptionFactory conflictFactory,
                                      DataValidationTaskInfoFactory dataValidationTaskInfoFactory) {
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
    }

    private QualityCodeSystem getQualityCodeSystemForApplication(String applicationName) {
        // TODO kore shouldn't know anything about applications, to be fixed
        return "MDC".equals(applicationName) ? QualityCodeSystem.MDC : QualityCodeSystem.MDM;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    @Transactional
    public Response createDataValidationTask(DataValidationTaskInfo info,
                                             @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        DataValidationTaskBuilder builder = validationService.newTaskBuilder()
                .setName(info.name)
                .setLogLevel(info.logLevelId == null ? TaskLogLevel.WARNING : TaskLogLevel.valueOf(info.logLevelId))
                .setQualityCodeSystem(getQualityCodeSystemForApplication(applicationName))
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun);
        if (info.deviceGroup != null) {
            builder = builder.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
        }
        if (info.usagePointGroup != null) {
            builder = builder.setUsagePointGroup(usagePointGroup(info.usagePointGroup.id));
        }
        DataValidationTask dataValidationTask = builder.create();

        return Response.status(Response.Status.CREATED).entity(dataValidationTaskInfoFactory.asInfo(dataValidationTask)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getDataValidationTasks(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                @BeanParam JsonQueryParameters queryParameters) {
        List<DataValidationTaskInfo> infos = validationService.findValidationTasks()
                .stream()
                .filter(task -> task.getQualityCodeSystem().equals(getQualityCodeSystemForApplication(applicationName)))
                .map(dataValidationTaskInfoFactory::asInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("dataValidationTasks", infos, queryParameters);
    }

    @DELETE @Transactional
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response deleteDataValidationTask(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                             @PathParam("dataValidationTaskId") long dataValidationTaskId,
                                             DataValidationTaskInfo info) {
        info.id = dataValidationTaskId;
        DataValidationTask task = findAndLockDataValidationTask(info, getQualityCodeSystemForApplication(applicationName));
        task.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public DataValidationTaskInfo getDataValidationTask(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                        @PathParam("dataValidationTaskId") long dataValidationTaskId,
                                                        @Context SecurityContext securityContext) {
        DataValidationTask task = findValidationTaskForQualitySystem(dataValidationTaskId, getQualityCodeSystemForApplication(applicationName))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return dataValidationTaskInfoFactory.asInfo(task);
    }

    @PUT
    @Transactional
    @Path("/{dataValidationTaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response updateReadingTypeDataValidationTask(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                        @PathParam("dataValidationTaskId") long dataValidationTaskId,
                                                        DataValidationTaskInfo info) {
        info.id = dataValidationTaskId;
        DataValidationTask task = findAndLockDataValidationTask(info, getQualityCodeSystemForApplication(applicationName));
        task.setName(info.name);
        task.setLogLevel(info.logLevelId == null ? TaskLogLevel.WARNING : TaskLogLevel.valueOf(info.logLevelId));
        task.setScheduleExpression(getScheduleExpression(info));
        if (info.deviceGroup != null) {
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
            task.setUsagePointGroup(null);
        }
        if (info.usagePointGroup != null) {
            task.setUsagePointGroup(usagePointGroup(info.usagePointGroup.id));
            task.setEndDeviceGroup(null);
        }
        if (info.deviceGroup == null && info.usagePointGroup == null) {
            task.setUsagePointGroup(null);
            task.setEndDeviceGroup(null);
        }
        task.setNextExecution(info.nextRun);
        task.update();
        return Response.ok(dataValidationTaskInfoFactory.asInfo(task)).build();
    }

    @PUT
    @Path("/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response triggerDataValidationTask(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                              @PathParam("id") long id,
                                              DataValidationTaskInfo info) {
        info.id = id;
        transactionService.execute(VoidTransaction.of(() -> validationService.findAndLockValidationTaskByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, info.name)
                        .withActualVersion(() -> findValidationTaskForQualitySystem(info.id, getQualityCodeSystemForApplication(applicationName))
                                .map(DataValidationTask::getVersion).orElse(null))
                        .supplier())
                .triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getDataValidationTaskHistory(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                                            @PathParam("id") long id,
                                                                            @Context SecurityContext securityContext,
                                                                            @BeanParam JsonQueryFilter filter,
                                                                            @BeanParam JsonQueryParameters queryParameters) {
        DataValidationTask task = fetchDataValidationTask(id, applicationName);
        DataValidationOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder()
                .setStart(queryParameters.getStart().orElse(0))
                .setLimit(queryParameters.getLimit().orElse(-1) + 1);

        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                occurrencesFinder.withStartDateIn(filter.getClosedRange("startedOnFrom", "startedOnTo"));
            } else {
                occurrencesFinder.withStartDateIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                occurrencesFinder.withEndDateIn(filter.getClosedRange("finishedOnFrom", "finishedOnTo"));
            } else {
                occurrencesFinder.withEndDateIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withEndDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }
        List<? extends DataValidationOccurrence> occurrences = occurrencesFinder.find();

        History<DataValidationTask> history = (History<DataValidationTask>) task.getHistory();
        List<DataValidationTaskHistoryInfo> infos = occurrences.stream()
                .map(occurrence -> dataValidationTaskInfoFactory.asInfo(history, occurrence))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", infos, queryParameters);
    }

    @GET
    @Path("/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public DataValidationOccurrenceLogInfos getDataValidationTaskHistory(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                                         @PathParam("id") long id,
                                                                         @PathParam("occurrenceId") long occurrenceId,
                                                                         @Context SecurityContext securityContext,
                                                                         @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        DataValidationTask task = fetchDataValidationTask(id, applicationName);
        DataValidationOccurrence occurrence = fetchDataValidationOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder()
                .setStart(queryParameters.getStartInt())
                .setLimit(queryParameters.getLimit());

        List<? extends LogEntry> occurrences = finder.find();

        DataValidationOccurrenceLogInfos infos = new DataValidationOccurrenceLogInfos(queryParameters.clipToLimit(occurrences), thesaurus);
        infos.total = queryParameters.determineTotal(occurrences.size());
        return infos;
    }

    private DataValidationTask fetchDataValidationTask(long id, String applicationName) {
        return findValidationTaskForQualitySystem(id, getQualityCodeSystemForApplication(applicationName)).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private DataValidationOccurrence fetchDataValidationOccurrence(long id, DataValidationTask task) {
        return task.getOccurrence(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElse(null);
    }

    private UsagePointGroup usagePointGroup(long usagePointGroupId) {
        return meteringGroupsService.findUsagePointGroup(usagePointGroupId).orElse(null);
    }

    private MetrologyContract metrologyContract(long metrologyContractId) {
        return metrologyConfigurationService.findMetrologyContract(metrologyContractId).orElse(null);
    }

    private ScheduleExpression getScheduleExpression(DataValidationTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private DataValidationTask findAndLockDataValidationTask(DataValidationTaskInfo info, QualityCodeSystem qualityCodeSystem) {
        return findAndLockValidationTaskByIdAndVersionForQualitySystem(info.id, info.version, qualityCodeSystem)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> findValidationTaskForQualitySystem(info.id, qualityCodeSystem).map(DataValidationTask::getVersion).orElse(null))
                        .supplier());
    }

    private Optional<DataValidationTask> findValidationTaskForQualitySystem(long id, QualityCodeSystem qualityCodeSystem) {
        return validationService.findValidationTask(id).filter(task -> task.getQualityCodeSystem().equals(qualityCodeSystem));
    }

    private Optional<DataValidationTask> findAndLockValidationTaskByIdAndVersionForQualitySystem(long id, long version, QualityCodeSystem qualityCodeSystem) {
        return validationService.findAndLockValidationTaskByIdAndVersion(id, version).filter(task -> task.getQualityCodeSystem().equals(qualityCodeSystem));
    }
}
