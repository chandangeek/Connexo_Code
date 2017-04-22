/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.conditions.Order;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/estimation")
public class EstimationResource {

    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";

    private final RestQueryService queryService;
    private final EstimationService estimationService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final EstimationRuleSetInfoFactory estimationRuleSetInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final EstimatorInfoFactory estimatorInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public EstimationResource(RestQueryService queryService, EstimationService estimationService, Thesaurus thesaurus, TimeService timeService,
                              MeteringGroupsService meteringGroupsService, MetrologyConfigurationService metrologyConfigurationService,
                              ConcurrentModificationExceptionFactory conflictFactory, PropertyValueInfoService propertyValueInfoService,
                              EstimationRuleSetInfoFactory estimationRuleSetInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory,
                              EstimatorInfoFactory estimatorInfoFactory, ResourceHelper resourceHelper) {
        this.queryService = queryService;
        this.estimationService = estimationService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.conflictFactory = conflictFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.estimationRuleSetInfoFactory = estimationRuleSetInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.estimatorInfoFactory = estimatorInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    private QualityCodeSystem getQualityCodeSystemFromApplicationName(String applicationName) {
        // TODO kore shouldn't know anything about applications, to be fixed
        return "MDC".equals(applicationName) ? QualityCodeSystem.MDC : QualityCodeSystem.MDM;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION
    })
    public PagedInfoList getEstimationRuleSets(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                               @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<EstimationRuleSetInfo> infos = queryRuleSets(params, getQualityCodeSystemFromApplicationName(applicationName)).stream()
                .map(estimationRuleSetInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("ruleSets", infos, queryParameters);
    }

    private List<EstimationRuleSet> queryRuleSets(QueryParameters queryParameters, QualityCodeSystem qualityCodeSystem) {
        Query<EstimationRuleSet> query = estimationService.getEstimationRuleSetQuery();
        query.setRestriction(where("qualityCodeSystem").isEqualTo(qualityCodeSystem));
        RestQuery<EstimationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @GET
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public EstimationRuleSetInfo getEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId) {
        EstimationRuleSet estimationRuleSet = resourceHelper.findEstimationRuleSetOrThrowException(ruleSetId);
        return estimationRuleSetInfoFactory.asFullInfo(estimationRuleSet);
    }

    @GET
    @Path("/{ruleSetId}/rules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION
    })
    public PagedInfoList getEstimationRules(@PathParam("ruleSetId") long ruleSetId, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet estimationRuleSet = resourceHelper.findEstimationRuleSetOrThrowException(ruleSetId);
        List<EstimationRuleInfo> infos = estimationRuleSet.getRules()
                .stream()
                .map(estimationRuleInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("rules", infos, queryParameters);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response createEstimationRuleSet(EstimationRuleSetInfo info, @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet(
                info.name, getQualityCodeSystemFromApplicationName(applicationName), info.description);
        EstimationRuleSetInfo estimationRuleSetInfo = estimationRuleSetInfoFactory.asInfo(estimationRuleSet);
        return Response.status(Response.Status.CREATED).entity(estimationRuleSetInfo).build();
    }

    @PUT
    @Path("/{ruleSetId}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleSetInfo updateEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, final EstimationRuleSetInfo info) {
        info.id = ruleSetId;
        EstimationRuleSet set = resourceHelper.findAndLockEstimationRuleSet(info);
        set.setName(info.name);
        set.setDescription(info.description);
        set.save();
        if (info.rules != null && !info.rules.isEmpty()) {
            long[] current = set.getRules().stream().mapToLong(EstimationRule::getId).toArray();
            long[] target = info.rules.stream().mapToLong(ruleInfo -> ruleInfo.id).toArray();
            KPermutation kPermutation = KPermutation.of(current, target);
            if (!kPermutation.isNeutral(set.getRules())) {
                set.reorderRules(kPermutation);
            }
        }
        return getEstimationRuleSet(ruleSetId);
    }

    @DELETE
    @Path("/{ruleSetId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, EstimationRuleSetInfo info) {
        info.id = ruleSetId;
        EstimationRuleSet ruleSet = resourceHelper.findAndLockEstimationRuleSet(info);
        if (estimationService.isEstimationRuleSetInUse(ruleSet)) {
            throw new EstimationRuleSetInUseLocalizedException(thesaurus, ruleSet);
        }
        ruleSet.delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/estimators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public PagedInfoList getAvailableEstimators(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                                @QueryParam("propertyDefinitionLevel") String propertyDefinitionLevel,
                                                @BeanParam JsonQueryParameters parameters) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);
        List<EstimatorInfo> data = estimationService.getAvailableEstimators(qualityCodeSystem).stream()
                .map(estimator ->
                        getEstimationPropertyDefinitionLevel(propertyDefinitionLevel)
                                .map(level -> estimatorInfoFactory.asInfo(estimator, level))
                                .orElseGet(() -> estimatorInfoFactory.asInfo(estimator)))
                .sorted(Comparator.comparing(estimatorInfo -> estimatorInfo.displayName.toLowerCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimators", data, parameters);
    }

    private Optional<EstimationPropertyDefinitionLevel> getEstimationPropertyDefinitionLevel(String propertyLevel) {
        return Optional.ofNullable(propertyLevel).map(EstimationPropertyDefinitionLevel::valueOf);
    }

    @GET
    @Path("/{ruleSetId}/usage")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public Response getValidationRuleSetUsage(@PathParam("ruleSetId") final long ruleSetId) {
        EstimationRuleSet estimationRuleSet = resourceHelper.findEstimationRuleSetOrThrowException(ruleSetId);
        RuleSetUsageInfo info = new RuleSetUsageInfo();
        info.isInUse = estimationService.isEstimationRuleSetInUse(estimationRuleSet);
        return Response.status(Response.Status.OK).entity(info).build();
    }

    class RuleSetUsageInfo {
        public boolean isInUse;
    }

    @POST
    @Path("/{ruleSetId}/rules")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response addRule(@PathParam("ruleSetId") long ruleSetId, EstimationRuleInfo info, @Context SecurityContext securityContext) {
        EstimationRuleSet ruleSet = resourceHelper.findEstimationRuleSetOrThrowException(ruleSetId);
        String[] readingTypes = info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).toArray(String[]::new);
        Estimator estimator = resourceHelper.findEstimatorOrThrowException(info.implementation);
        EstimationRuleBuilder estimationRuleBuilder = ruleSet.addRule(info.implementation, info.name).withReadingType(readingTypes);
        estimator.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE)
                .forEach(propertySpec -> {
                    Object value = propertyValueInfoService.findPropertyValue(propertySpec, info.properties);
                    estimationRuleBuilder.havingProperty(propertySpec.getName()).withValue(value);
                });
        estimationRuleBuilder.active(false);
        EstimationRule rule = estimationRuleBuilder.create();
        return Response.status(Response.Status.CREATED).entity(estimationRuleInfoFactory.asInfo(rule)).build();
    }

    @PUT
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleInfo editRule(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleId") long ruleId, EstimationRuleInfo info) {
        info.id = ruleId;
        EstimationRule rule = resourceHelper.findAndLockRule(info);
        List<String> mRIDs = info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).collect(Collectors.toList());
        Map<String, Object> propertyMap = new HashMap<>();
        try {
            for (PropertySpec propertySpec : rule.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE)) {
                Object value = propertyValueInfoService.findPropertyValue(propertySpec, info.properties);
                propertyMap.put(propertySpec.getName(), value);
            }
        } finally {
            rule = rule.getRuleSet().updateRule(info.id, info.name, info.active, mRIDs, propertyMap);
        }
        return estimationRuleInfoFactory.asInfo(rule);
    }

    @GET
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION
    })
    public Response getRule(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleId") long ruleId) {
        EstimationRuleSet ruleSet = resourceHelper.findEstimationRuleSetOrThrowException(ruleSetId);
        EstimationRule rule = resourceHelper.findEstimationRuleInRuleSetOrThrowException(ruleSet, ruleId);
        return Response.ok(estimationRuleInfoFactory.asInfo(rule)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeRule(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleId") long ruleId, EstimationRuleInfo info) {
        info.id = ruleId;
        EstimationRule estimationRule = resourceHelper.findAndLockRule(info);
        estimationRule.getRuleSet().deleteRule(estimationRule);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public PagedInfoList getEstimationTasks(@Context UriInfo uriInfo,
                                            @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                            @BeanParam JsonQueryParameters queryParameters) {
        List<EstimationTaskInfo> infos = estimationService.findEstimationTasks(getQualityCodeSystemFromApplicationName(applicationName))
                .stream()
                .map(et -> new EstimationTaskInfo(et, thesaurus, timeService))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("estimationTasks", infos, queryParameters);
    }

    @GET
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public EstimationTaskInfo getEstimationTask(@PathParam("id") long id,
                                                @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                                @Context SecurityContext securityContext) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);
        return new EstimationTaskInfo(fetchEstimationTask(id, qualityCodeSystem), thesaurus, timeService);
    }

    @PUT
    @Path("/tasks/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK})
    public Response triggerEstimationTask(EstimationTaskInfo info, @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);
        findAndLockEstimationTaskByIdAndVersionInApplication(info.id, info.version, qualityCodeSystem)
                .orElseThrow(conflictFactory.conflict()
                        .withMessageTitle(MessageSeeds.RUN_TASK_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.RUN_TASK_CONCURRENT_BODY, info.name)
                        .withActualVersion(() -> findEstimationTaskInApplication(info.id, qualityCodeSystem).map(EstimationTask::getVersion).orElse(null))
                        .supplier())
                .triggerNow();
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response addEstimationTask(EstimationTaskInfo info, @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        EstimationTask dataExportTask = estimationService.newBuilder()
                .setName(info.name)
                .setLogLevel(info.logLevel)
                .setRevalidate(info.revalidate)
                .setQualityCodeSystem(qualityCodeSystem)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun))
                .setPeriod(getRelativePeriod(info.period))
                .setEndDeviceGroup(info.deviceGroup != null ? endDeviceGroup(info.deviceGroup.id) : null)
                .setUsagePointGroup(info.usagePointGroup != null ? usagePointGroup(info.usagePointGroup.id) : null)
                .setMetrologyPurpose(info.metrologyPurpose != null ? metrologyPurpose(info.metrologyPurpose.id) : null)
                .create();

        return Response.status(Response.Status.CREATED)
                .entity(new EstimationTaskInfo(dataExportTask, thesaurus, timeService))
                .build();

    }

    @DELETE
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeEstimationTask(@PathParam("id") long id, EstimationTaskInfo info,
                                         @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        String taskName = "id = " + id;
        try {
            EstimationTask task = findAndLockTask(info, qualityCodeSystem);
            taskName = task.getName();
            task.delete();
            return Response.status(Response.Status.OK).build();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.DELETE_TASK_SQL_EXCEPTION, "status", taskName);
        }
    }

    @PUT
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK})
    public Response updateEstimationTask(@PathParam("id") long id, EstimationTaskInfo info,
                                         @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        EstimationTask task = findAndLockTask(info, qualityCodeSystem);
        task.setName(info.name);
        task.setLogLevel(info.logLevel);
        task.setRevalidate(info.revalidate);
        task.setScheduleExpression(getScheduleExpression(info));
        if (Never.NEVER.equals(task.getScheduleExpression())) {
            task.setNextExecution(null);
        } else {
            task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
        }
        task.setPeriod(getRelativePeriod(info.period));

        if (info.deviceGroup != null) {
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
        }
        if (info.usagePointGroup != null) {
            task.setUsagePointGroup(usagePointGroup(info.usagePointGroup.id));
            if (info.metrologyPurpose != null) {
                task.setMetrologyPurpose(metrologyPurpose(info.metrologyPurpose.id));
            }
        }
        if (info.usagePointGroup == null && info.deviceGroup == null) {
            task.setEndDeviceGroup(null);
            task.setUsagePointGroup(null);
        }

        task.update();
        return Response.status(Response.Status.OK).entity(new EstimationTaskInfo(task, thesaurus, timeService)).build();

    }

    @GET
    @Path("/tasks/{id}/history")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
            Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK
    })
    public PagedInfoList getEstimationTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                  @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters parameters,
                                                  @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        EstimationTask task = fetchEstimationTask(id, qualityCodeSystem);
        EstimationTaskOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder().setStart(parameters.getStart().orElse(0)).setLimit(parameters.getLimit().orElse(10) + 1);

        if (filter.hasProperty("startedOnFrom")) {
            occurrencesFinder.withStartDateIn(filter.hasProperty("startedOnTo") ?
                    Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")) : Range.atLeast(filter.getInstant("startedOnFrom")));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }

        if (filter.hasProperty("finishedOnFrom")) {
            occurrencesFinder.withEndDateIn(filter.hasProperty("finishedOnTo") ?
                    Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")) : Range.atLeast(filter.getInstant("finishedOnFrom")));
        } else if (filter.hasProperty("finishedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }

        List<EstimationTaskHistoryInfo> historyList = occurrencesFinder.find()
                .stream()
                .map(occurrence -> new EstimationTaskHistoryInfo(task, occurrence, thesaurus))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", historyList, parameters);
    }

    @GET
    @Path("/tasks/{id}/history/{occurrenceId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
            Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK
    })
    public PagedInfoList getEstimationTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                  @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName,
                                                  @BeanParam JsonQueryParameters parameters) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        EstimationTask task = fetchEstimationTask(id, qualityCodeSystem);
        TaskOccurrence occurrence = fetchTaskOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder().setStart(parameters.getStart().orElse(0)).setLimit(parameters.getLimit().orElse(10) + 1);

        List<EstimationTaskOccurrenceLogInfo> taskOccurrenceLogsList = finder.find()
                .stream()
                .map(EstimationTaskOccurrenceLogInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", taskOccurrenceLogsList, parameters);
    }

    private EstimationTask findAndLockTask(EstimationTaskInfo info, QualityCodeSystem qualityCodeSystem) {
        return findAndLockEstimationTaskByIdAndVersionInApplication(info.id, info.version, qualityCodeSystem)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> findEstimationTaskInApplication(info.id, qualityCodeSystem).map(EstimationTask::getVersion).orElse(null))
                        .supplier());
    }

    private ScheduleExpression getScheduleExpression(EstimationTaskInfo info) {
        return info.schedule == null ? Never.NEVER : info.schedule.toExpression();
    }

    private EndDeviceGroup endDeviceGroup(long endDeviceGroupId) {
        return meteringGroupsService.findEndDeviceGroup(endDeviceGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private UsagePointGroup usagePointGroup(long usagePointGroupId) {
        return meteringGroupsService.findUsagePointGroup(usagePointGroupId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private MetrologyPurpose metrologyPurpose(long metrologyPurposeId) {
        if (metrologyPurposeId != 0) {
            return metrologyConfigurationService.findMetrologyPurpose(metrologyPurposeId)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        } else {
            return null;
        }
    }

    private RelativePeriod getRelativePeriod(RelativePeriodInfo relativePeriodInfo) {
        if (relativePeriodInfo == null) {
            return null;
        }
        return timeService.findRelativePeriod(relativePeriodInfo.id).orElse(null);
    }

    private EstimationTask fetchEstimationTask(long id, QualityCodeSystem qualityCodeSystem) {
        return findEstimationTaskInApplication(id, qualityCodeSystem)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private TaskOccurrence fetchTaskOccurrence(long occurrenceId, EstimationTask task) {
        return task.getOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private Optional<? extends EstimationTask> findEstimationTaskInApplication(long id, QualityCodeSystem qualityCodeSystem) {
        return estimationService.findEstimationTask(id).filter(task -> task.getQualityCodeSystem().equals(qualityCodeSystem));
    }

    private Optional<? extends EstimationTask> findAndLockEstimationTaskByIdAndVersionInApplication(long id, long version, QualityCodeSystem qualityCodeSystem) {
        return estimationService.findAndLockEstimationTask(id, version).filter(task -> task.getQualityCodeSystem().equals(qualityCodeSystem));
    }
}
