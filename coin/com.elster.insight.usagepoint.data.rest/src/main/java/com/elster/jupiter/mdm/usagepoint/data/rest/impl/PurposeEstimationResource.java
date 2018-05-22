/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.EstimationStatusInfo;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.rest.EstimationRuleSetInfo;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurposeEstimationResource {

    private final ResourceHelper resourceHelper;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final EstimationService estimationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    PurposeEstimationResource(ResourceHelper resourceHelper,
                              UsagePointConfigurationService usagePointConfigurationService,
                              EstimationService estimationService,
                              ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public Response getEstimationRuleSets(@PathParam("name") String name,
                                          @PathParam("purposeId") long contractId,
                                          @BeanParam JsonQueryParameters queryParameters) {

        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);

        List<PurposeEstimationRuleSetInfo> ruleSetInfos = new ArrayList<>();
        List<EstimationRuleSet> linkedRuleSets = usagePointConfigurationService.getEstimationRuleSets(metrologyContract);
        if (!linkedRuleSets.isEmpty()) {
            List<EstimationRuleSet> activeRuleSets = currentEffectiveMC.getChannelsContainer(metrologyContract)
                    .map(estimationService::activeRuleSets)
                    .orElse(Collections.emptyList());

            for (EstimationRuleSet ruleSet : linkedRuleSets) {
                boolean isActive = activeRuleSets.contains(ruleSet);
                ruleSetInfos.add(new PurposeEstimationRuleSetInfo(ruleSet, isActive));
            }
            ruleSetInfos.sort(EstimationRuleSetInfo.ESTIMATION_RULESET_NAME_COMPARATOR);
        }
        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(ruleSetInfos).from(queryParameters).find(), queryParameters)).build();
    }

    @Path("/{estimationRuleSetId}/status")
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION})
    public Response setEstimationRuleSetStatus(@PathParam("name") String name,
                                               @PathParam("purposeId") long contractId,
                                               @PathParam("estimationRuleSetId") long estimationRuleSetId,
                                               PurposeEstimationRuleSetInfo info) {
        ChannelsContainer channelsContainer = this.getChannelsContainer(name, contractId);
        EstimationRuleSet ruleSet = this.getEstimationRuleSet(estimationRuleSetId);
        this.setEstimationRuleSetActivationStatus(channelsContainer, ruleSet, info.isActive);
        return Response.ok().build();
    }



    @Path("/estimationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION,
            Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public Response getEstimationStatus(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        ChannelsContainer channelsContainer = this.getChannelsContainer(name, contractId);
        boolean status = estimationService.isEstimationActive(channelsContainer);
        EstimationStatusInfo estimationStatusInfo = new EstimationStatusInfo(status);
        return Response.ok(estimationStatusInfo).build();
    }


    @Path("/estimationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION})
    public Response setEstimationStatus(@PathParam("name") String name,
                                        @PathParam("purposeId") long contractId,
                                        EstimationStatusInfo info) {
        ChannelsContainer channelsContainer = this.getChannelsContainer(name, contractId);
        if (info.active){
            estimationService.activateEstimation(channelsContainer);
        } else {
            estimationService.deactivateEstimation(channelsContainer);
        }
        return Response.ok().build();
    }

    private EstimationRuleSet getEstimationRuleSet(long estimationRuleSetId) {
        return estimationService.getEstimationRuleSet(estimationRuleSetId)
                .filter(ruleSet -> QualityCodeSystem.MDM.equals(ruleSet.getQualityCodeSystem()))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void setEstimationRuleSetActivationStatus(ChannelsContainer channelsContainer, EstimationRuleSet ruleSet, boolean status) {
        if (status) {
            estimationService.activate(channelsContainer, ruleSet);
        } else {
            estimationService.deactivate(channelsContainer, ruleSet);
        }
    }

    private ChannelsContainer getChannelsContainer(String name, long contractId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        return currentEffectiveMC.getChannelsContainer(metrologyContract)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGY_CONTRACT_NOT_LINKED_TO_CHANNELS_CONTAINER, metrologyContract.getId()));
    }
}
