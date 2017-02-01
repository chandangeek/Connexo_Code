/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
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
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
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
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;

    @Inject
    public EstimationResource(RestQueryService queryService,
                              EstimationService estimationService,
                              TransactionService transactionService,
                              Thesaurus thesaurus,
                              TimeService timeService,
                              MeteringGroupsService meteringGroupsService,
                              MetrologyConfigurationService metrologyConfigurationService, ConcurrentModificationExceptionFactory conflictFactory,
                              PropertyValueInfoService propertyValueInfoService,
                              EstimationRuleInfoFactory estimationRuleInfoFactory) {
        this.queryService = queryService;
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.conflictFactory = conflictFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
    }

    private QualityCodeSystem getQualityCodeSystemFromApplicationName(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        // TODO kore shouldn't know anything about applications, to be fixed
        return "MDC".equals(applicationName) ? QualityCodeSystem.MDC : QualityCodeSystem.MDM;
    }

    /**
     * Get all estimation rulesets
     *
     * @param uriInfo uriInfo containing queryparameters
     * @return all estimation rulesets
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public EstimationRuleSetInfos getEstimationRuleSets(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<EstimationRuleSet> list = queryRuleSets(params, getQualityCodeSystemFromApplicationName(applicationName));

        EstimationRuleSetInfos infos = new EstimationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<EstimationRuleSet> queryRuleSets(QueryParameters queryParameters, QualityCodeSystem qualityCodeSystem) {
        Query<EstimationRuleSet> query = estimationService.getEstimationRuleSetQuery();
        query.setRestriction(where("qualityCodeSystem").isEqualTo(qualityCodeSystem));
        RestQuery<EstimationRuleSet> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @GET
    @Path("/{ruleSetId}/rules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public EstimationRuleInfos getEstimationRules(@PathParam("ruleSetId") long ruleSetId, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Optional<? extends EstimationRuleSet> optional = estimationService.getEstimationRuleSet(ruleSetId);
        if (optional.isPresent()) {
            EstimationRuleInfos infos = new EstimationRuleInfos();
            EstimationRuleSet set = optional.get();
            List<? extends EstimationRule> rules;
            if ((params.getLimit() > 0) && (params.getStartInt() >= 0)) {
                rules = set.getRules(params.getStartInt(), params.getLimit());
            } else {
                rules = set.getRules();
            }
            for (EstimationRule rule : rules) {
                infos.add(estimationRuleInfoFactory.createEstimationRuleInfo(rule));
            }
            infos.total = set.getRules().size();
            return infos;
        } else {
            return new EstimationRuleInfos();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response createEstimationRuleSet(final EstimationRuleSetInfo info, @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        return Response.status(Response.Status.CREATED)
                .entity(new EstimationRuleSetInfo(transactionService.execute(
                        () -> estimationService.createEstimationRuleSet(info.name, getQualityCodeSystemFromApplicationName(applicationName), info.description))))
                .build();
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleSetInfo updateEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, final EstimationRuleSetInfo info) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                EstimationRuleSet set = findAndLockRuleSet(info);
                set.setName(info.name);
                set.setDescription(info.description);
                set.save();
                if ((info.rules != null) && (!info.rules.isEmpty())) {
                    long[] current = set.getRules().stream()
                            .mapToLong(EstimationRule::getId)
                            .toArray();
                    long[] target = info.rules.stream()
                            .mapToLong(ruleInfo -> ruleInfo.id)
                            .toArray();
                    KPermutation kPermutation = KPermutation.of(current, target);
                    if (!kPermutation.isNeutral(set.getRules())) {
                        set.reorderRules(kPermutation);
                    }
                }
            }
        });
        return getEstimationRuleSet(ruleSetId);
    }

    @GET
    @Path("/{ruleSetId}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public EstimationRuleSetInfo getEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId) {
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId);
        return EstimationRuleSetInfo.withRules(estimationRuleSet, estimationRuleInfoFactory);
    }

    private EstimationRuleSet fetchEstimationRuleSet(long id) {
        return estimationService.getEstimationRuleSet(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/estimators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public PagedInfoList getAvailableEstimators(@HeaderParam(APPLICATION_HEADER_PARAM) String applicationName, @BeanParam JsonQueryParameters parameters) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);
        List<EstimationInfo> data = estimationService.getAvailableEstimators(qualityCodeSystem).stream()
                .sorted(Compare.BY_DISPLAY_NAME)
                .map(estimator -> new EstimationInfo(
                        estimator.getClass().getName(),
                        estimator.getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(estimator.getPropertySpecs())))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimators", data, parameters);
    }

    class RuleSetUsageInfo {
        public boolean isInUse;
    }

    @GET
    @Path("/{ruleSetId}/usage")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public Response getValidationRuleSetUsage(@PathParam("ruleSetId") final long ruleSetId) {
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId);
        RuleSetUsageInfo info = new RuleSetUsageInfo();
        info.isInUse = estimationService.isEstimationRuleSetInUse(estimationRuleSet);
        return Response.status(Response.Status.OK).entity(info).build();
    }

    @DELETE
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response deleteEstimationRuleSet(@PathParam("ruleSetId") final long ruleSetId, EstimationRuleSetInfo info) {
        EstimationRuleSet ruleSet = findAndLockRuleSet(info);
        if (estimationService.isEstimationRuleSetInUse(ruleSet) && ruleSet.getQualityCodeSystem()
                .equals(QualityCodeSystem.MDM)) {
            throw new EstimationRuleSetInUseLocalizedException(thesaurus, ruleSet);
        } else {
            transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    ruleSet.delete();
                }
            });
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{ruleSetId}/rules")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response addRule(@PathParam("ruleSetId") final long ruleSetId, final EstimationRuleInfo info, @Context SecurityContext securityContext) {
        EstimationRuleInfo result =
                transactionService.execute(() -> {
                    EstimationRuleSet set = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                    EstimationRuleBuilder estimationRuleBuilder = set.addRule(info.implementation, info.name)
                            .withReadingType(info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).toArray(String[]::new));
                    Estimator estimator = estimationService.getEstimator(info.implementation).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                    estimator.getPropertySpecs()
                            .forEach(propertySpec -> {
                                Object value = propertyValueInfoService.findPropertyValue(propertySpec, info.properties);
                                estimationRuleBuilder.havingProperty(propertySpec.getName()).withValue(value);
                            });
                    estimationRuleBuilder.active(false);
                    EstimationRule rule = estimationRuleBuilder.create();
                    return estimationRuleInfoFactory.createEstimationRuleInfo(rule);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleInfos editRule(final EstimationRuleInfo info, @Context SecurityContext securityContext) {
        EstimationRuleInfos result = new EstimationRuleInfos();
        result.add(estimationRuleInfoFactory.createEstimationRuleInfo(transactionService.execute(() -> {
            EstimationRule rule = findAndLockRule(info);
            List<String> mRIDs = info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).collect(Collectors.toList());
            Map<String, Object> propertyMap = new HashMap<>();
            try {
                for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                    Object value = propertyValueInfoService.findPropertyValue(propertySpec, info.properties);
                    propertyMap.put(propertySpec.getName(), value);
                }
            } finally {
                rule = rule.getRuleSet().updateRule(info.id, info.name, info.active, mRIDs, propertyMap);
            }

            return rule;
        })));
        return result;
    }

    @GET
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public Response getRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId) {
        EstimationRule rule = transactionService.execute((Transaction<EstimationRule>) () -> {
            EstimationRuleSet ruleSet = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

            return getEstimationRuleFromSetOrThrowException(ruleSet, ruleId);
        });
        return Response.ok(estimationRuleInfoFactory.createEstimationRuleInfo(rule)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeRule(EstimationRuleInfo info) {
        transactionService.execute(() -> {
            EstimationRule estimationRule = findAndLockRule(info);
            estimationRule.getRuleSet().deleteRule(estimationRule);
            return null;
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }


    private enum Compare implements Comparator<Estimator> {
        BY_DISPLAY_NAME;

        @Override
        public int compare(Estimator o1, Estimator o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
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
                .setQualityCodeSystem(qualityCodeSystem)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun))
                .setPeriod(getRelativePeriod(info.period))
                .setEndDeviceGroup(info.deviceGroup!=null ? endDeviceGroup(info.deviceGroup.id) : null)
                .setUsagePointGroup(info.usagePointGroup!=null ? usagePointGroup(info.usagePointGroup.id) : null)
                .setMetrologyPurpose(info.metrologyPurpose!=null ? metrologyPurpose(info.metrologyPurpose.id) : null)
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
        task.setScheduleExpression(getScheduleExpression(info));
        if (Never.NEVER.equals(task.getScheduleExpression())) {
            task.setNextExecution(null);
        } else {
            task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
        }
        task.setPeriod(getRelativePeriod(info.period));

        if(info.deviceGroup!=null) {
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));
        }
        if(info.usagePointGroup!=null){
            task.setUsagePointGroup(usagePointGroup(info.usagePointGroup.id));
            if(info.metrologyPurpose!=null){
                task.setMetrologyPurpose(metrologyPurpose(info.metrologyPurpose.id));
            }
        }
        if(info.usagePointGroup==null && info.deviceGroup==null){
            task.setEndDeviceGroup(null);
            task.setUsagePointGroup(null);
        }

        task.update();
        return Response.status(Response.Status.OK).entity(new EstimationTaskInfo(task, thesaurus, timeService)).build();

    }

    @GET
    @Path("/tasks/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public PagedInfoList getEstimationTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                  @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters parameters,
                                                  @HeaderParam(APPLICATION_HEADER_PARAM) String applicationName) {
        QualityCodeSystem qualityCodeSystem = getQualityCodeSystemFromApplicationName(applicationName);

        EstimationTask task = fetchEstimationTask(id, qualityCodeSystem);
        EstimationTaskOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder().setStart(parameters.getStart().orElse(0)).setLimit(parameters.getLimit().orElse(10) + 1);

        if (filter.hasProperty("startedOnFrom")) {
            occurrencesFinder.withStartDateIn(filter.hasProperty("startedOnTo") ? Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")) : Range.atLeast(filter.getInstant("startedOnFrom")));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }

        if (filter.hasProperty("finishedOnFrom")) {
            occurrencesFinder.withEndDateIn(filter.hasProperty("finishedOnTo") ? Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")) : Range.atLeast(filter.getInstant("finishedOnFrom")));
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
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK,
            Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK,
            Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
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

    private EstimationRuleSet findAndLockRuleSet(EstimationRuleSetInfo info) {
        return estimationService.findAndLockEstimationRuleSet(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> estimationService.getEstimationRuleSet(info.id)
                                .filter(candidate -> candidate.getObsoleteDate() == null)
                                .map(EstimationRuleSet::getVersion)
                                .orElse(null))
                        .supplier());
    }

    private EstimationRule findAndLockRule(EstimationRuleInfo info) {
        Optional<? extends EstimationRuleSet> ruleSet = estimationService.findAndLockEstimationRuleSet(info.parent.id, info.parent.version);
        Long actualRuleVersion = null;
        Long actualParentVersion = null;
        if (!ruleSet.isPresent()) { // parent was changed or deleted
            Optional<? extends EstimationRuleSet> unlockedRuleSet = estimationService.getEstimationRuleSet(info.parent.id);
            // if rule set was deleted, the rule should be deleted as well, so both should have the 'null' version
            if (unlockedRuleSet.isPresent() && unlockedRuleSet.get().getObsoleteDate() == null) {
                actualParentVersion = unlockedRuleSet.get().getVersion();
                actualRuleVersion = getCurrentEstimationRuleVersion(info, unlockedRuleSet);
            }
        } else { // no changes in parent
            actualParentVersion = ruleSet.get().getVersion();
            Optional<? extends EstimationRule> estimationRule = estimationService.findAndLockEstimationRule(info.id, info.version);
            if (!estimationRule.isPresent()) { // but rule itself was changed
                actualRuleVersion = getCurrentEstimationRuleVersion(info, ruleSet);
            } else { // no changes in rule
                return estimationRule.get();
            }
        }
        Long ruleVersion = actualRuleVersion;
        Long parentVersion = actualParentVersion;
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualVersion(() -> ruleVersion)
                .withActualParent(() -> parentVersion, info.parent.id)
                .build();
    }

    private Long getCurrentEstimationRuleVersion(EstimationRuleInfo info, Optional<? extends EstimationRuleSet> ruleSet) {
        return ruleSet.get().getRules().stream()
                .filter(input -> input.getId() == info.id && !input.isObsolete())
                .findAny()
                .map(EstimationRule::getVersion)
                .orElse(null);
    }

    private EstimationRule getEstimationRuleFromSetOrThrowException(EstimationRuleSet ruleSet, long ruleId) {
        return ruleSet.getRules().stream()
                .filter(input -> input.getId() == ruleId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
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
