/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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


    @Inject
    PurposeValidationResource(UsagePointConfigurationService usagePointConfigurationService,
                              ResourceHelper resourceHelper,
                              ValidationService validationService,
                              ValidationStatusFactory validationStatusFactory) {
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.validationStatusFactory = validationStatusFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationRuleSetsForPurpose(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
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


    @GET
    @Path("/validationstatus")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public Response getValidationStatus(@PathParam("name") String name, @PathParam("purposeId") long contractId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = resourceHelper.findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = resourceHelper.findMetrologyContractOrThrowException(currentEffectiveMC, contractId);

        UsagePointValidationStatusInfo validationStatusInfo = currentEffectiveMC.getChannelsContainer(metrologyContract)
                .map(channelsContainer -> validationStatusFactory.getValidationStatusInfo(currentEffectiveMC, metrologyContract, channelsContainer))
                .orElse(new UsagePointValidationStatusInfo());

        return Response.ok(validationStatusInfo).build();
    }
}
