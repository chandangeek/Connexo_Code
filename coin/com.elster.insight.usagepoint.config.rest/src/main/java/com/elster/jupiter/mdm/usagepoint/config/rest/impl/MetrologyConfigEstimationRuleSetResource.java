package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.security.Privileges;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.IdWithNameInfo;
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
import java.util.Set;
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
        EstimationRuleSet foundValidationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<LinkableMetrologyContractInfo> infos = usagePointConfigurationService.getMetrologyContractsLinkedToEstimationnRuleSet(foundValidationRuleSet)
                .stream()
                .map(metrologyContract -> getLinkableMetrologyContractInfo(metrologyContract, foundValidationRuleSet))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", infos, queryParameters);
    }


    @DELETE
    @Path("/{estimationRuleSetId}/purposes/{metrologyContractId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response removeMetrologyConfigurationPurpose(@PathParam("estimationRuleSetId") long estimationRuleSetId, @PathParam("metrologyContractId") long metrologyContractId, LinkableMetrologyContractInfo metrologyContractInfo, @BeanParam JsonQueryParameters queryParameters) {
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
        List<LinkableMetrologyContractInfo> linkablePurposes = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .flatMap(metrologyConfiguration -> metrologyConfiguration.getContracts().stream())
                .filter(metrologyContract -> usagePointConfigurationService.isLinkableEstimationRuleSet(metrologyContract, estimationRuleSet, usagePointConfigurationService
                        .getEstimationRuleSets(metrologyContract)))
                .map(metrologyContract -> getLinkableMetrologyContractInfo(metrologyContract, estimationRuleSet))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("purposes", linkablePurposes, queryParameters);
    }

    @PUT
    @Path("/{estimationRuleSetId}/purposes/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ESTIMATION_ON_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_ESTIMATION_ON_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response linkMetrologyPurposeToValidationRuleSet(@PathParam("estimationRuleSetId") long estimationRuleSetId, MetrologyContractInfos metrologyContractInfos, @BeanParam JsonQueryParameters queryParameters) {
        EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        metrologyContractInfos.contracts.forEach(metrologyContractInfo -> {
            MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo);
            usagePointConfigurationService.addEstimationRuleSet(metrologyContract, estimationRuleSet);
        });
        return Response.status(Response.Status.OK).build();
    }

    private List<OutputMatchesInfo> getMatchedOutputs(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet) {
        Set<ReadingType> readingTypes = estimationRuleSet.getRules()
                .stream()
                .flatMap(rule -> rule.getReadingTypes().stream())
                .collect(Collectors.toSet());
        return metrologyContract.getDeliverables()
                .stream()
                .map(deliverable -> new OutputMatchesInfo(deliverable.getName(), readingTypes.contains(deliverable.getReadingType())))
                .sorted(Comparator.comparing((outputMatchesInfo -> !outputMatchesInfo.isMatched)))
                .collect(Collectors.toList());
    }

    private LinkableMetrologyContractInfo getLinkableMetrologyContractInfo(MetrologyContract contract, EstimationRuleSet ruleSet) {
        LinkableMetrologyContractInfo info = new LinkableMetrologyContractInfo();
        info.setMetrologyConfigurationInfo(new IdWithNameInfo(contract.getMetrologyConfiguration().getId(),
                contract.getMetrologyConfiguration().getName()));
        info.setActive(contract.getMetrologyConfiguration().isActive());
        info.setOutputs(getMatchedOutputs(contract, ruleSet));
        info.setVersion(contract.getVersion());
        info.setId(contract.getId());
        info.setName(contract.getMetrologyPurpose().getName());

        return info;
    }
}