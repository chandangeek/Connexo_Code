package com.elster.jupiter.estimation.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/estimation")
public class EstimationResource {

    private final RestQueryService queryService;
    private final EstimationService estimationService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final MeteringGroupsService meteringGroupsService;
    private final PropertyUtils propertyUtils;

    @Inject
    public EstimationResource(RestQueryService queryService, EstimationService estimationService, TransactionService transactionService, Thesaurus thesaurus, TimeService timeService, MeteringGroupsService meteringGroupsService, PropertyUtils propertyUtils) {
        this.queryService = queryService;
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.meteringGroupsService = meteringGroupsService;
        this.propertyUtils = propertyUtils;
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
    public EstimationRuleSetInfos getEstimationRuleSets(@Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<EstimationRuleSet> list = queryRuleSets(params);

        EstimationRuleSetInfos infos = new EstimationRuleSetInfos(params.clipToLimit(list));
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<EstimationRuleSet> queryRuleSets(QueryParameters queryParameters) {
        Query<EstimationRuleSet> query = estimationService.getEstimationRuleSetQuery();
        query.setRestriction(where("obsoleteTime").isNull());
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
            EstimationRuleInfos infos = new EstimationRuleInfos(propertyUtils);
            EstimationRuleSet set = optional.get();
            List<? extends EstimationRule> rules;
            if ((params.getLimit() > 0) && (params.getStartInt() >= 0)) {
                rules = set.getRules(params.getStartInt(), params.getLimit());
            } else {
                rules = set.getRules();
            }
            for (EstimationRule rule : rules) {
                infos.add(rule);
            }
            infos.total = set.getRules().size();
            return infos;
        } else {
            return new EstimationRuleInfos(propertyUtils);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response createEstimationRuleSet(final EstimationRuleSetInfo info) {
        return Response.status(Response.Status.CREATED).entity(new EstimationRuleSetInfo(transactionService.execute(new Transaction<EstimationRuleSet>() {
            @Override
            public EstimationRuleSet perform() {
                return estimationService.createEstimationRuleSet(info.name, info.description);
            }
        }))).build();
    }

    @PUT
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleSetInfo updateEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, final EstimationRuleSetInfo info, @Context SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                estimationService.getEstimationRuleSet(ruleSetId).ifPresent(set -> {
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
                });
            }
        });
        return getEstimationRuleSet(ruleSetId, securityContext);
    }

    @GET
    @Path("/{ruleSetId}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public EstimationRuleSetInfo getEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, @Context SecurityContext securityContext) {
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId, securityContext);
        return EstimationRuleSetInfo.withRules(estimationRuleSet, propertyUtils);
    }

    private EstimationRuleSet fetchEstimationRuleSet(long id, SecurityContext securityContext) {
        return estimationService.getEstimationRuleSet(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{ruleSetId}/rule/{ruleId}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public ReadingTypeInfos getReadingTypesForRule(@PathParam("ruleSetId") long ruleSetId, @PathParam("ruleId") long ruleId, @Context SecurityContext securityContext) {
        ReadingTypeInfos infos = new ReadingTypeInfos();
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId, securityContext);
        EstimationRule estimationRule = getEstimationRuleFromSetOrThrowException(estimationRuleSet, ruleId);
        Set<ReadingType> readingTypes = estimationRule.getReadingTypes();
        for (ReadingType readingType : readingTypes) {
            infos.add(readingType);
        }
        infos.total = readingTypes.size();
        return infos;
    }

    @GET
    @Path("/estimators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public EstimatorInfos getAvailableEstimatimators(@Context UriInfo uriInfo) {
        EstimatorInfos infos = new EstimatorInfos();
        List<Estimator> toAdd = estimationService.getAvailableEstimators();
        Collections.sort(toAdd, Compare.BY_DISPLAY_NAME);
        for (Estimator estimator : toAdd) {
            infos.add(estimator.getClass().getName(), estimator.getDisplayName(), propertyUtils.convertPropertySpecsToPropertyInfos(estimator.getPropertySpecs()));
        }
        infos.total = toAdd.size();
        return infos;
    }

    class RuleSetUsageInfo {
        public boolean isInUse;
    }

    @GET
    @Path("/{ruleSetId}/usage")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public Response getValidationRuleSetUsage(@PathParam("ruleSetId") final long ruleSetId, @Context final SecurityContext securityContext) {
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId, securityContext);
        RuleSetUsageInfo info = new RuleSetUsageInfo();
        info.isInUse = estimationService.isEstimationRuleSetInUse(estimationRuleSet);
        return Response.status(Response.Status.OK).entity(info).build();
    }

    @DELETE
    @Path("/{ruleSetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response deleteEstimationRuleSet(@PathParam("ruleSetId") final long ruleSetId, @Context final SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                estimationService.getEstimationRuleSet(ruleSetId).
                        orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)).
                        delete();
            }
        });
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
                    EstimationRule rule = set.addRule(info.implementation, info.name);
                    for (ReadingTypeInfo readingTypeInfo : info.readingTypes) {
                        rule.addReadingType(readingTypeInfo.mRID);
                    }
                    try {
                        for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                            Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                            rule.addProperty(propertySpec.getName(), value);
                        }

                    } catch (EstimatorNotFoundException ex) {
                    } finally {
                        set.save();
                    }
                    return new EstimationRuleInfo(rule, propertyUtils);
                });
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleInfos editRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId, final EstimationRuleInfo info, @Context SecurityContext securityContext) {
        EstimationRuleInfos result = new EstimationRuleInfos(propertyUtils);
        result.add(transactionService.execute(() -> {
            EstimationRuleSet ruleSet = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

            EstimationRule rule = getEstimationRuleFromSetOrThrowException(ruleSet, ruleId);

            List<String> mRIDs = info.readingTypes.stream().map(readingTypeInfo -> readingTypeInfo.mRID).collect(Collectors.toList());
            Map<String, Object> propertyMap = new HashMap<>();
            for (PropertySpec propertySpec : rule.getPropertySpecs()) {
                Object value = propertyUtils.findPropertyValue(propertySpec, info.properties);
                if (value != null) {
                    propertyMap.put(propertySpec.getName(), value);
                }
            }
            rule = ruleSet.updateRule(ruleId, info.name, info.active, mRIDs, propertyMap);
            return rule;
        }));
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
        return Response.ok(new EstimationRuleInfo(rule, propertyUtils)).build();
    }

    @DELETE
    @Path("/{ruleSetId}/rules/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeRule(@PathParam("ruleSetId") final long ruleSetId, @PathParam("ruleId") final long ruleId) {
        transactionService.execute(new Transaction<EstimationRule>() {
            @Override
            public EstimationRule perform() {
                Optional<? extends EstimationRuleSet> ruleSetRef = estimationService.getEstimationRuleSet(ruleSetId);
                if (!ruleSetRef.isPresent() || ruleSetRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                Optional<? extends EstimationRule> ruleRef = estimationService.getEstimationRule(ruleId);
                if (!ruleRef.isPresent() || ruleRef.get().getObsoleteDate() != null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                ruleSetRef.get().deleteRule(ruleRef.get());
                return null;
            }
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

    private EstimationRule getEstimationRuleFromSetOrThrowException(EstimationRuleSet ruleSet, long ruleId) {
        return ruleSet.getRules().stream()
                .filter(input -> input.getId() == ruleId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public EstimationTaskInfos getEstimationTasks(@Context UriInfo uriInfo) {

        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<? extends EstimationTask> list = queryTasks(params);

        EstimationTaskInfos infos = new EstimationTaskInfos(params.clipToLimit(list), thesaurus);
        infos.total = params.determineTotal(list.size());

        return infos;
    }

    private List<? extends EstimationTask> queryTasks(QueryParameters queryParameters) {
        Query<? extends EstimationTask> query = estimationService.getEstimationTaskQuery();
        RestQuery<? extends EstimationTask> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.descending("lastRun").nullsLast(), Order.ascending("name"));
    }

    @GET
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public EstimationTaskInfo getEstimationTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return new EstimationTaskInfo(fetchEstimationTask(id), thesaurus);
    }

    @POST
    @Path("/tasks/{id}/trigger")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK})
    public Response triggerEstimationTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        transactionService.execute(VoidTransaction.of(() -> fetchEstimationTask(id).triggerNow()));
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response addEstimationTask(EstimationTaskInfo info) {

        EstimationTask dataExportTask = estimationService.newBuilder()
                .setName(info.name)
                .setScheduleExpression(getScheduleExpression(info))
                .setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun))
                .setPeriod(getRelativePeriod(info.period))
                .setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id)).build();
        try (TransactionContext context = transactionService.getContext()) {
            dataExportTask.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new EstimationTaskInfo(dataExportTask, thesaurus)).build();
    }

    @DELETE
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public Response removeEstimationTask(@PathParam("id") long id, @Context SecurityContext securityContext) {
        EstimationTask task = fetchEstimationTask(id);

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
    @Path("/tasks/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK})
    public Response updateEstimationTask(@PathParam("id") long id, EstimationTaskInfo info) {

        EstimationTask task = findTaskOrThrowException(id);

        try (TransactionContext context = transactionService.getContext()) {
            task.setName(info.name);
            task.setScheduleExpression(getScheduleExpression(info));
            if (Never.NEVER.equals(task.getScheduleExpression())) {
                task.setNextExecution(null);
            } else {
                task.setNextExecution(info.nextRun == null ? null : Instant.ofEpochMilli(info.nextRun));
            }
            task.setPeriod(getRelativePeriod(info.period));
            task.setEndDeviceGroup(endDeviceGroup(info.deviceGroup.id));

            task.save();
            context.commit();
        }
        return Response.status(Response.Status.CREATED).entity(new EstimationTaskInfo(task, thesaurus)).build();
    }

    @GET
    @Path("/tasks/{id}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public PagedInfoList getEstimationTaskHistory(@PathParam("id") long id, @Context SecurityContext securityContext,
                                                  @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters parameters) {

        EstimationTask task = fetchEstimationTask(id);
        EstimationTaskOccurrenceFinder occurrencesFinder = task.getOccurrencesFinder().setStart(parameters.getStart().orElse(0)).setLimit(parameters.getLimit().orElse(10) + 1);

        if (filter.hasProperty("startedOnFrom")) {
            occurrencesFinder.withStartDateIn(Range.closed(filter.getInstant("startedOnFrom"), filter.hasProperty("startedOnTo") ? filter.getInstant("startedOnTo") : Instant.now()));
        } else if (filter.hasProperty("startedOnTo")) {
            occurrencesFinder.withStartDateIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }

        if (filter.hasProperty("finishedOnFrom")) {
            occurrencesFinder.withEndDateIn(Range.closed(filter.getInstant("finishedOnFrom"), filter.hasProperty("finishedOnTo") ? filter.getInstant("finishedOnTo") : Instant.now()));
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
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_ESTIMATION_CONFIGURATION, Privileges.Constants.UPDATE_SCHEDULE_ESTIMATION_TASK, Privileges.Constants.RUN_ESTIMATION_TASK, Privileges.Constants.VIEW_ESTIMATION_TASK, Privileges.Constants.ADMINISTRATE_ESTIMATION_TASK})
    public PagedInfoList getEstimationTaskHistory(@PathParam("id") long id, @PathParam("occurrenceId") long occurrenceId,
                                                  @Context SecurityContext securityContext, @BeanParam JsonQueryParameters parameters) {

        EstimationTask task = fetchEstimationTask(id);
        TaskOccurrence occurrence = fetchTaskOccurrence(occurrenceId, task);
        LogEntryFinder finder = occurrence.getLogsFinder().setStart(parameters.getStart().orElse(0)).setLimit(parameters.getLimit().orElse(10) + 1);

        List<EstimationTaskOccurrenceLogInfo> taskOccurrenceLogsList = finder.find()
                .stream()
                .map(EstimationTaskOccurrenceLogInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", taskOccurrenceLogsList, parameters);
    }

    private EstimationTask findTaskOrThrowException(long id) {
        return estimationService.findEstimationTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private ScheduleExpression getScheduleExpression(EstimationTaskInfo info) {
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

    private EstimationTask fetchEstimationTask(long id) {
        return estimationService.findEstimationTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private TaskOccurrence fetchTaskOccurrence(long occurrenceId, EstimationTask task) {
        return task.getOccurrence(occurrenceId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
