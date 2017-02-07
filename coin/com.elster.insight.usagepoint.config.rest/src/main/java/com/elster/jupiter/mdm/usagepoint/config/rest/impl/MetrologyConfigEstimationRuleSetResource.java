/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/estimationrulesets")
public class MetrologyConfigEstimationRuleSetResource {

    private final EstimationService estimationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final ResourceHelper resourceHelper;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigEstimationRuleSetResource(EstimationService estimationService, UsagePointConfigurationService usagePointConfigurationService, ResourceHelper resourceHelper, MetrologyConfigurationService metrologyConfigurationService) {
        this.estimationService = estimationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.resourceHelper = resourceHelper;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }


    @GET
    @Path("/{estimationRuleSetId}/purposes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkedMetrologyConfigurationPurposes(@PathParam("estimationRuleSetId") long estimationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet foundEstimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<LinkableMetrologyContractInfo> infos = usagePointConfigurationService.getMetrologyContractsLinkedToEstimationRuleSet(foundEstimationRuleSet)
                .stream()
                .map(metrologyContract ->  {
                    List<ReadingTypeDeliverable> matchedDeliverables = usagePointConfigurationService.getMatchingDeliverablesOnEstimationRuleSet(metrologyContract, foundEstimationRuleSet);
                    return resourceHelper.getLinkableMetrologyContractInfo(metrologyContract, metrologyContract.getDeliverables()
                            .stream()
                            .map(deliverable -> new OutputMatchesInfo(deliverable.getName(), matchedDeliverables.contains(deliverable)))
                            .sorted(Comparator.comparing(outputMatchesInfo -> !outputMatchesInfo.isMatched))
                            .collect(Collectors.toList()));
                }).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", infos, queryParameters);
    }


    @DELETE
    @Path("/{estimationRuleSetId}/purposes/{metrologyContractId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response removeMetrologyConfigurationPurpose(@PathParam("estimationRuleSetId") long estimationRuleSetId, @PathParam("metrologyContractId") long metrologyContractId, LinkableMetrologyContractInfo metrologyContractInfo) {
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo.getId(), metrologyContractInfo.getVersion(), metrologyContractInfo.getName());
        usagePointConfigurationService.removeEstimationRuleSet(metrologyContract, estimationRuleSet);
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{estimationRuleSetId}/purposes/overview")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkablePurposes(@PathParam("estimationRuleSetId") long estimationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<MetrologyContract> linkedPurposes = usagePointConfigurationService.getMetrologyContractsLinkedToEstimationRuleSet(estimationRuleSet);
        List<LinkableMetrologyContractInfo> purposes = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .flatMap(metrologyConfiguration -> metrologyConfiguration.getContracts().stream())
                .filter(metrologyContract -> !usagePointConfigurationService.getMatchingDeliverablesOnEstimationRuleSet(metrologyContract, estimationRuleSet).isEmpty())
                .filter(metrologyContract -> !linkedPurposes.contains(metrologyContract))
                .map(metrologyContract -> {
                    List<ReadingTypeDeliverable> matchedDeliverables = usagePointConfigurationService.getMatchingDeliverablesOnEstimationRuleSet(metrologyContract, estimationRuleSet);
                    List<OutputMatchesInfo> outputMatchesInfos = metrologyContract.getDeliverables()
                            .stream()
                            .map(deliverable -> new OutputMatchesInfo(deliverable.getName(), matchedDeliverables.contains(deliverable)))
                            .sorted(Comparator.comparing((outputMatchesInfo -> !outputMatchesInfo.isMatched)))
                            .collect(Collectors.toList());
                    return resourceHelper.getLinkableMetrologyContractInfo(metrologyContract, outputMatchesInfos);
                }).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", purposes, queryParameters);
    }

    @PUT
    @Path("/{estimationRuleSetId}/purposes/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response linkMetrologyPurposesToEstimationRuleSet(@PathParam("estimationRuleSetId") long estimationRuleSetId, MetrologyContractInfos metrologyContractInfos) {
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        metrologyContractInfos.contracts.forEach(metrologyContractInfo -> {
            MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo);
            usagePointConfigurationService.addEstimationRuleSet(metrologyContract, estimationRuleSet);
        });
        return Response.status(Response.Status.OK).build();
    }
}