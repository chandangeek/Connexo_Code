package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.rest.MetrologyConfigValidationRuleSetInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.OutputMatchesInfo;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/validationruleset")
public class MetrologyConfigValidationRuleSetResource {

    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigValidationRuleSetResource(ValidationService validationService, UsagePointConfigurationService usagePointConfigurationService, MetrologyConfigurationService metrologyConfigurationService) {
        this.validationService = validationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Path("/{validationRuleSetId}/purposes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkedMetrologyConfigurationPurposes(@PathParam("validationRuleSetId") long validationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        Optional<? extends ValidationRuleSet> foundValidationRuleSet = validationService.getValidationRuleSet(validationRuleSetId);
        List<MetrologyConfigValidationRuleSetInfo> infos = new ArrayList<>();
        if (foundValidationRuleSet.isPresent()) {
            ValidationRuleSet ruleSet = foundValidationRuleSet.get();
            List<MetrologyContract> contracts = usagePointConfigurationService.getMetrologyContractsLinkedToValidationRuleSet(ruleSet);
            contracts.forEach(contract -> infos.add(getMetrologyConfigurationInfo(contract, ruleSet)));
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return PagedInfoList.fromPagedList("purposes", infos, queryParameters);
    }

    @DELETE
    @Path("/{validationRuleSetId}/purposes/{metrologyContractId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response removeMetrologyConfigurationPurpose(@PathParam("validationRuleSetId") long validationRuleSetId, @PathParam("metrologyContractId") long metrologyContractId, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        MetrologyContract metrologyContract = metrologyConfigurationService.findMetrologyContract(metrologyContractId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        usagePointConfigurationService.removeValidationRuleSet(metrologyContract, validationRuleSet);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{validationRuleSetId}/purposes/overview")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkablePurposes(@PathParam("validationRuleSetId") long validationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<MetrologyConfigValidationRuleSetInfo> availableOutputs = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .flatMap(metrologyConfiguration -> metrologyConfiguration.getContracts().stream())
                .flatMap(metrologyContract -> getAvailablePurposes(metrologyContract, validationRuleSet).stream())
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", availableOutputs, queryParameters);
    }

    @PUT
    @Path("/{validationRuleSetId}/purposes/add")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response linkMetrologyPurposeToValidationRuleSet(@PathParam("validationRuleSetId") long validationRuleSetId, int[] metrologyContractIds, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        Arrays.stream(metrologyContractIds).forEach(metrologyContractId -> {
            MetrologyContract metrologyContract = metrologyConfigurationService.findMetrologyContract(metrologyContractId)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            usagePointConfigurationService.addValidationRuleSet(metrologyContract, validationRuleSet);
        });

        return Response.status(Response.Status.OK).build();
    }

    private List<MetrologyConfigValidationRuleSetInfo> getAvailablePurposes(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet) {
        List<MetrologyConfigValidationRuleSetInfo> metrologyContracts = new ArrayList<>();
        if (usagePointConfigurationService.isLinkableValidationRuleSet(metrologyContract, validationRuleSet, usagePointConfigurationService
                .getValidationRuleSets(metrologyContract))) {
            metrologyContracts.add(getMetrologyConfigurationInfo(metrologyContract, validationRuleSet));
        }

        return metrologyContracts;
    }

    private List<OutputMatchesInfo> getMatchedOutputs(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet) {
        List<OutputMatchesInfo> matches = new ArrayList<>();
        validationRuleSet.getRules().stream().flatMap(rule -> rule.getReadingTypes().stream())
                .forEach(readingType -> metrologyContract.getDeliverables().forEach(deliverable ->
                        matches.add(new OutputMatchesInfo(deliverable.getName(), deliverable.getReadingType()
                                .equals(readingType)))));
        return matches;
    }

    private MetrologyConfigValidationRuleSetInfo getMetrologyConfigurationInfo(MetrologyContract contract, ValidationRuleSet ruleSet) {
        MetrologyConfigValidationRuleSetInfo info = new MetrologyConfigValidationRuleSetInfo();
        info.setMetrologyConfigurationInfo(new IdWithNameInfo(contract.getMetrologyConfiguration()
                .getId(), contract.getMetrologyConfiguration().getName()));
        info.setActive(contract.getMetrologyConfiguration().isActive());
        info.setPurpose(contract.getMetrologyPurpose().getName());
        info.setOutputs(getMatchedOutputs(contract, ruleSet));
        info.setMetrologyContractId(contract.getId());

        return info;
    }
}
