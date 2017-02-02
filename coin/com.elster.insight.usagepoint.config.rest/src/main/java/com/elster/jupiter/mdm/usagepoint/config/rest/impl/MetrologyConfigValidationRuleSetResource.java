package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

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

@Path("/validationrulesets")
public class MetrologyConfigValidationRuleSetResource {

    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public MetrologyConfigValidationRuleSetResource(ValidationService validationService, UsagePointConfigurationService usagePointConfigurationService, MetrologyConfigurationService metrologyConfigurationService, ResourceHelper resourceHelper) {
        this.validationService = validationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Path("/{validationRuleSetId}/purposes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkedMetrologyConfigurationPurposes(@PathParam("validationRuleSetId") long validationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet foundValidationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<LinkableMetrologyContractInfo> infos = usagePointConfigurationService.getMetrologyContractsLinkedToValidationRuleSet(foundValidationRuleSet)
                .stream()
                .map(metrologyContract ->  {
                    List<ReadingTypeDeliverable> matchedDeliverables = usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, foundValidationRuleSet);
                    return getLinkableMetrologyContractInfo(metrologyContract, metrologyContract.getDeliverables()
                            .stream()
                            .map(deliverable -> new OutputMatchesInfo(deliverable.getName(), matchedDeliverables.contains(deliverable)))
                            .sorted(Comparator.comparing(outputMatchesInfo -> !outputMatchesInfo.isMatched))
                            .collect(Collectors.toList()));
                }).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", infos, queryParameters);
    }

    @DELETE
    @Path("/{validationRuleSetId}/purposes/{metrologyContractId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response removeMetrologyConfigurationPurpose(@PathParam("validationRuleSetId") long validationRuleSetId, @PathParam("metrologyContractId") long metrologyContractId, LinkableMetrologyContractInfo metrologyContractInfo, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo.getId(), metrologyContractInfo.getVersion(), metrologyContractInfo.getName());

        usagePointConfigurationService.removeValidationRuleSet(metrologyContract, validationRuleSet);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{validationRuleSetId}/purposes/overview")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getLinkablePurposes(@PathParam("validationRuleSetId") long validationRuleSetId, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<LinkableMetrologyContractInfo> linkablePurposes = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .flatMap(metrologyConfiguration -> metrologyConfiguration.getContracts().stream())
                .filter(metrologyContract -> !usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, validationRuleSet).isEmpty())
                .filter(metrologyContract -> !usagePointConfigurationService.getMetrologyContractsLinkedToValidationRuleSet(validationRuleSet).contains(metrologyContract))
                .map(metrologyContract -> {
                    List<ReadingTypeDeliverable> matchedDeliverables = usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, validationRuleSet);
                    List<OutputMatchesInfo> outputMatchesInfos = metrologyContract.getDeliverables()
                            .stream()
                            .map(deliverable -> new OutputMatchesInfo(deliverable.getName(), matchedDeliverables.contains(deliverable)))
                            .sorted(Comparator.comparing((outputMatchesInfo -> !outputMatchesInfo.isMatched)))
                            .collect(Collectors.toList());
                    return getLinkableMetrologyContractInfo(metrologyContract, outputMatchesInfos);
                }).collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", linkablePurposes, queryParameters);
    }

    @PUT
    @Path("/{validationRuleSetId}/purposes/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_VALIDATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response linkMetrologyPurposeToValidationRuleSet(@PathParam("validationRuleSetId") long validationRuleSetId, MetrologyContractInfos metrologyContractInfos, @BeanParam JsonQueryParameters queryParameters) {
        ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        metrologyContractInfos.contracts.forEach(metrologyContractInfo -> {
            MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo);
            usagePointConfigurationService.addValidationRuleSet(metrologyContract, validationRuleSet);
        });

        return Response.status(Response.Status.OK).build();
    }

    private LinkableMetrologyContractInfo getLinkableMetrologyContractInfo(MetrologyContract contract, List<OutputMatchesInfo> matchedOutputs) {
        LinkableMetrologyContractInfo info = new LinkableMetrologyContractInfo();
        info.setMetrologyConfigurationInfo(new IdWithNameInfo(contract.getMetrologyConfiguration().getId(),
                contract.getMetrologyConfiguration().getName()));
        info.setActive(contract.getMetrologyConfiguration().isActive());
        info.setOutputs(matchedOutputs);
        info.setVersion(contract.getVersion());
        info.setId(contract.getId());
        info.setName(contract.getMetrologyPurpose().getName());

        return info;
    }
}
