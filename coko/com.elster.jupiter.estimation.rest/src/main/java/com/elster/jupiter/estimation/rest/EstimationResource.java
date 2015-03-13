package com.elster.jupiter.estimation.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/estimation")
public class EstimationResource {

    private final RestQueryService queryService;
    private final EstimationService estimationService;
    private final TransactionService transactionService;

    @Inject
    public EstimationResource(RestQueryService queryService, EstimationService estimationService, TransactionService transactionService) {
        this.queryService = queryService;
        this.estimationService = estimationService;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION,
            Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public EstimationRuleInfos getEstimationRules(@PathParam("ruleSetId") long ruleSetId, @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        Optional<? extends EstimationRuleSet> optional = estimationService.getEstimationRuleSet(ruleSetId);
        if (optional.isPresent()) {
            EstimationRuleInfos infos = new EstimationRuleInfos();
            EstimationRuleSet set = optional.get();
            List<? extends EstimationRule> rules;
            if (params.size() == 0) {
                rules = set.getRules();
            } else {
                rules = set.getRules(params.getStart(), params.getLimit());
            }
            for (EstimationRule rule : rules) {
                infos.add(rule);
            }
            infos.total = set.getRules().size();
            return infos;
        } else {
            return new EstimationRuleInfos();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION)
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION)
    public EstimationRuleSetInfo updateEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, final EstimationRuleSetInfo info, @Context SecurityContext securityContext) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                estimationService.getEstimationRuleSet(ruleSetId).ifPresent(set -> {
                    set.setName(info.name);
                    set.setDescription(info.description);
                    set.save();
                });
            }
        });
        return getEstimationRuleSet(ruleSetId, securityContext);
    }

    @GET
    @Path("/{ruleSetId}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION})
    public EstimationRuleSetInfo getEstimationRuleSet(@PathParam("ruleSetId") long ruleSetId, @Context SecurityContext securityContext) {
        EstimationRuleSet estimationRuleSet = fetchEstimationRuleSet(ruleSetId, securityContext);
        return new EstimationRuleSetInfo(estimationRuleSet);
    }

    private EstimationRuleSet fetchEstimationRuleSet(long id, SecurityContext securityContext) {
        return estimationService.getEstimationRuleSet(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{ruleSetId}/rule/{ruleId}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.VIEW_ESTIMATION_CONFIGURATION})
    public EstimatorInfos getAvailableEstimatimators(@Context UriInfo uriInfo) {
        EstimatorInfos infos = new EstimatorInfos();
        List<Estimator> toAdd = estimationService.getAvailableEstimators();
        Collections.sort(toAdd, Compare.BY_DISPLAY_NAME);
        PropertyUtils propertyUtils = new PropertyUtils();
        for (Estimator estimator : toAdd) {
            infos.add(estimator.getClass().getName(), estimator.getDisplayName(), propertyUtils.convertPropertySpecsToPropertyInfos(estimator.getPropertySpecs()));
        }
        infos.total = toAdd.size();
        return infos;
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




}
