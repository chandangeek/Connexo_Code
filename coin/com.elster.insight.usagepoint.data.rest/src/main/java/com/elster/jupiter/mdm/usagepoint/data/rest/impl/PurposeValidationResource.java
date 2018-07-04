/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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

public class PurposeValidationResource {

    private final UsagePointConfigurationService usagePointConfigurationService;
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final ValidationStatusFactory validationStatusFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    PurposeValidationResource(UsagePointConfigurationService usagePointConfigurationService,
                              ResourceHelper resourceHelper,
                              ValidationService validationService,
                              ValidationStatusFactory validationStatusFactory,
                              ExceptionFactory exceptionFactory) {
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.validationStatusFactory = validationStatusFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationRuleSets(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);

        List<PurposeValidationRuleSetInfo> ruleSetInfos = new ArrayList<>();
        List<ValidationRuleSet> linkedRuleSets = usagePointConfigurationService.getValidationRuleSets(metrologyContract);
        if (!linkedRuleSets.isEmpty()) {
            List<ValidationRuleSet> activeRuleSets = currentEffectiveMC.getChannelsContainer(metrologyContract)
                    .map(validationService::activeRuleSets)
                    .orElse(Collections.emptyList());
            for (ValidationRuleSet ruleSet : linkedRuleSets) {
                boolean isActive = activeRuleSets.contains(ruleSet);
                ruleSetInfos.add(new PurposeValidationRuleSetInfo(ruleSet, isActive));
            }
            ruleSetInfos.sort(ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        }

        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(ruleSetInfos).from(queryParameters).find(), queryParameters)).build();
    }

    @Path("/{validationRuleSetId}/status")
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response setValidationRuleSetStatus(@PathParam("name") String name,
                                               @PathParam("purposeId") long contractId,
                                               @PathParam("validationRuleSetId") long validationRuleSetId,
                                               PurposeValidationRuleSetInfo info) {
        ChannelsContainer channelsContainer = this.getChannelsContainer(name, contractId);
        ValidationRuleSet ruleSet = this.getValidationRuleSet(validationRuleSetId);
        this.setValidationRuleSetActivationStatus(channelsContainer, ruleSet, info.isActive);
        return Response.ok(new PurposeValidationRuleSetInfo(ruleSet, info.isActive)).build();
    }

    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        return validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void setValidationRuleSetActivationStatus(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet, boolean status) {
        if (status) {
            validationService.activate(channelsContainer, ruleSet);
        } else {
            validationService.deactivate(channelsContainer, ruleSet);
        }
    }

    @Path("/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationStatus(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        ChannelsContainer channelsContainer = resourceHelper.findChannelsContainerOrThrowException(currentEffectiveMC, metrologyContract);
        UsagePointValidationStatusInfo validationStatusInfo = validationStatusFactory.getValidationStatusInfo(currentEffectiveMC, metrologyContract, channelsContainer);
        return Response.ok(validationStatusInfo).build();
    }


    @Path("/validationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response setValidationStatus(@PathParam("name") String name,
                                        @PathParam("purposeId") long contractId,
                                        UsagePointValidationStatusInfo info) {
        ChannelsContainer channelsContainer = this.getChannelsContainer(name, contractId);
        if (info.validationActive){
            validationService.activateValidation(channelsContainer);
        } else {
            validationService.deactivateValidation(channelsContainer);
        }
        return Response.ok().build();
    }

    private ChannelsContainer getChannelsContainer(String name, long contractId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);
        return resourceHelper.findChannelsContainerOrThrowException(currentEffectiveMC, metrologyContract);
    }
}
