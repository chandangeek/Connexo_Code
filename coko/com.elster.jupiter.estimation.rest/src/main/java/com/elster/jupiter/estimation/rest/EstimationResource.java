package com.elster.jupiter.estimation.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Optional;

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
    /*@RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})*/
    public EstimationRuleSetInfos getValidationRuleSets(@Context UriInfo uriInfo) {
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
    /*@RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION,
            Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})*/
    public EstimationRuleInfos getValidationRules(@PathParam("ruleSetId") long ruleSetId, @Context UriInfo uriInfo) {
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



}
