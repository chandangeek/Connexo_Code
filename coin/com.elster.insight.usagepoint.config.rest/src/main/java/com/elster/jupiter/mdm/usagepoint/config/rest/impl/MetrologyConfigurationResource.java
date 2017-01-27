package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final MeteringService meteringService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private final ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final DataValidationTaskInfoFactory dataValidationTaskInfoFactory;

    @Inject
    public MetrologyConfigurationResource(ResourceHelper resourceHelper, MeteringService meteringService, UsagePointConfigurationService usagePointConfigurationService, ValidationService validationService, EstimationService estimationService,
                                          CustomPropertySetService customPropertySetService, CustomPropertySetInfoFactory customPropertySetInfoFactory, MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory,
                                          MetrologyConfigurationService metrologyConfigurationService, ReadingTypeDeliverableFactory readingTypeDeliverableFactory, DataValidationTaskInfoFactory dataValidationTaskInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
        this.dataValidationTaskInfoFactory = dataValidationTaskInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointMetrologyConfiguration> allMetrologyConfigurations =
                metrologyConfigurationService
                        .findAllMetrologyConfigurations()
                        .stream()
                        .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                        .map(UsagePointMetrologyConfiguration.class::cast)
                        .collect(Collectors.toList());
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).find()
                .stream()
                .map(metrologyConfigurationInfoFactory::asDetailedInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo getMetrologyConfiguration(@PathParam("id") long id) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }

    @GET
    @Path("/{id}/deliverables")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationDeliverables(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        List<ReadingTypeDeliverablesInfo> deliverables =  metrologyConfiguration.getDeliverables().stream().map(readingTypeDeliverableFactory::asInfo).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deliverables", deliverables, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response createMetrologyConfiguration(MetrologyConfigurationInfo metrologyConfigurationInfo) {
        /*
        Just a stub, not to break a possibility to create metrology configuration from UI
         */
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration(metrologyConfigurationInfo.name, serviceCategory)
                .create();
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response updateMetrologyConfiguration(MetrologyConfigurationInfo info, @Context SecurityContext securityContext) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        info.updateCustomPropertySets(metrologyConfiguration, resourceHelper::getRegisteredCustomPropertySetOrThrowException);
        return Response.ok().entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}/activate")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo activateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        metrologyConfiguration.activate();
        return metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration);
    }

    @PUT
    @Path("/{id}/deprecate")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo deprecateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        metrologyConfiguration.deprecate();
        return metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response deleteMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        UsagePointMetrologyConfiguration mc = resourceHelper.findAndLockMetrologyConfiguration(info);
        if (checkIfInUse(mc)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        mc.delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private boolean checkIfInUse(MetrologyConfiguration mc) {
        return usagePointConfigurationService.isInUse(mc);
    }

    @GET
    @Path("/{id}/contracts")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurationPurposes(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        List<MetrologyContractInfo> metrologyContractInfos = new ArrayList<>();
        for (MetrologyContract metrologyContract : resourceHelper.getMetrologyConfigOrThrowException(id).getContracts()) {
            List<ValidationRuleSetInfo> validationRuleSetInfos = new ArrayList<>();
            if (!usagePointConfigurationService.getValidationRuleSets(metrologyContract).isEmpty()) {
                for (ValidationRuleSet validationRuleSet : usagePointConfigurationService.getValidationRuleSets(metrologyContract)) {
                    ValidationRuleSetVersion activeRuleSetVersion = validationRuleSet.getRuleSetVersions()
                            .stream()
                            .filter(validationRuleSetVersion -> validationRuleSetVersion.getStatus() == ValidationVersionStatus.CURRENT)
                            .findFirst()
                            .get();
                    ValidationRuleSetInfo validationRuleSetInfo = new ValidationRuleSetInfo(validationRuleSet, activeRuleSetVersion);
                    validationRuleSetInfos.add(validationRuleSetInfo);
                }
            }
            List<EstimationRuleSetInfo> estimationRuleSetInfos = usagePointConfigurationService
                    .getEstimationRuleSets(metrologyContract)
                    .stream()
                    .map(EstimationRuleSetInfo::new)
                    .collect(Collectors.toList());
            MetrologyContractInfo metrologyContractInfo = new MetrologyContractInfo(metrologyContract);
            metrologyContractInfo.validationRuleSets = validationRuleSetInfos;
            metrologyContractInfo.estimationRuleSets = estimationRuleSetInfos;
            metrologyContractInfos.add(metrologyContractInfo);
        }
        Collections.sort(metrologyContractInfos, (MetrologyContractInfo a, MetrologyContractInfo b) -> a.name.compareTo(b.name));
        return PagedInfoList.fromCompleteList("contracts", metrologyContractInfos, queryParameters);
    }

    @PUT
    @Path("/{id}/contracts/{contractId}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response updateMetrologyContractWithValidationRuleSets(@PathParam("contractId") long contractId, @QueryParam("action") String action, MetrologyContractInfo metrologyContractInfo) {
        metrologyContractInfo.id = contractId;
        MetrologyContract metrologyContract = resourceHelper.findAndLockContractOnMetrologyConfiguration(metrologyContractInfo);
        if (action != null && action.equals("remove")) {
            if (!metrologyContractInfo.validationRuleSets.isEmpty()) {
                ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(metrologyContractInfo.validationRuleSets
                        .stream()
                        .findFirst()
                        .get().id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                usagePointConfigurationService.removeValidationRuleSet(metrologyContract, validationRuleSet);
            } else if (!metrologyContractInfo.estimationRuleSets.isEmpty()) {
                EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(metrologyContractInfo.estimationRuleSets
                        .stream()
                        .findFirst()
                        .get().id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                usagePointConfigurationService.removeEstimationRuleSet(metrologyContract, estimationRuleSet);
            }
        } else {
            for (ValidationRuleSetInfo validationRuleSetInfo : metrologyContractInfo.validationRuleSets) {
                ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(validationRuleSetInfo.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                usagePointConfigurationService.addValidationRuleSet(metrologyContract, validationRuleSet);
            }
            for (EstimationRuleSetInfo estimationRuleSetInfo : metrologyContractInfo.estimationRuleSets) {
                EstimationRuleSet estimationRuleSet = estimationService.getEstimationRuleSet(estimationRuleSetInfo.id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
                usagePointConfigurationService.addEstimationRuleSet(metrologyContract, estimationRuleSet);
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/contracts/{contractId}")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyContractInfo getLinkableValidationRuleSetsForMetrologyContract(@PathParam("contractId") long contractId) {
        MetrologyContract metrologyContract = resourceHelper.findContractByIdOrThrowException(contractId);
        List<ValidationRuleSetInfo> linkableValidationRuleSets = validationService.getValidationRuleSets()
                .stream()
                .filter(validationRuleSet -> validationRuleSet.getQualityCodeSystem().equals(QualityCodeSystem.MDM))
                .filter(validationRuleSet -> usagePointConfigurationService.isLinkableValidationRuleSet(metrologyContract, validationRuleSet,
                        usagePointConfigurationService.getValidationRuleSets(metrologyContract)))
                .map(ValidationRuleSetInfo::new)
                .collect(Collectors.toList());
        List<EstimationRuleSetInfo> linkableEstimationRuleSets = estimationService.getEstimationRuleSets()
                .stream()
                .filter(estimationRuleSet -> estimationRuleSet.getQualityCodeSystem().equals(QualityCodeSystem.MDM))
                .filter(estimationRuleSet -> usagePointConfigurationService.isLinkableEstimationRuleSet(metrologyContract, estimationRuleSet,
                        usagePointConfigurationService.getEstimationRuleSets(metrologyContract)))
                .map(EstimationRuleSetInfo::new)
                .collect(Collectors.toList());
        MetrologyContractInfo metrologyContractInfo = new MetrologyContractInfo(metrologyContract);
        metrologyContractInfo.validationRuleSets = linkableValidationRuleSets;
        metrologyContractInfo.estimationRuleSets = linkableEstimationRuleSets;
        return metrologyContractInfo;
    }

    @PUT
    @Path("/{id}/contracts")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response reorderEstimationRuleSetsOnContracts(@PathParam("contractId") long contractId, @QueryParam("action") String action, MetrologyContractInfos metrologyContractInfos) {
        for (MetrologyContractInfo contract : metrologyContractInfos.contracts) {
            MetrologyContract metrologyContract = resourceHelper.findContractByIdOrThrowException(contract.id);
            usagePointConfigurationService.reorderEstimationRuleSets(metrologyContract, contract.estimationRuleSets.stream()
                    .map(estimationRuleSetInfo -> estimationService.getEstimationRuleSet(estimationRuleSetInfo.id))
                    .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigCustomPropertySets(@PathParam("id") long id,
                                                              @QueryParam("linked") @DefaultValue("true") boolean linked,
                                                              @BeanParam JsonQueryParameters queryParameters) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        Stream<RegisteredCustomPropertySet> customPropertySets = metrologyConfiguration.getCustomPropertySets().stream();
        if (!linked) {
            Set<String> assignedCPSIds = customPropertySets
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .map(CustomPropertySet::getId)
                    .collect(Collectors.toSet());

            Set<String> serviceCatCPSIds =
                    Arrays.stream(ServiceKind.values())
                            .map(meteringService::getServiceCategory)
                            .flatMap(Functions.asStream())
                            .flatMap(sc -> sc.getCustomPropertySets().stream())
                            .map(RegisteredCustomPropertySet::getCustomPropertySet)
                            .map(CustomPropertySet::getId)
                            .collect(Collectors.toSet());

            customPropertySets = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)
                    .stream()
                    .filter(cps -> !assignedCPSIds.contains(cps.getCustomPropertySet().getId()))
                    .filter(cps -> !serviceCatCPSIds.contains(cps.getCustomPropertySet().getId()));
        }
        List<?> infos = customPropertySets
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .sorted((a, b) -> a.getCustomPropertySet().getName().compareToIgnoreCase(b.getCustomPropertySet().getName()))
                .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }

    @PUT
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo addCustomPropertySetToMetrologyConfiguration(@PathParam("id") long id,
                                                                                   MetrologyConfigurationInfo info) {
        info.id = id;
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        if (info.customPropertySets != null) {
            info.customPropertySets
                    .stream()
                    .map(cpsInfo -> resourceHelper.getRegisteredCustomPropertySetOrThrowException(cpsInfo.customPropertySetId))
                    .forEach(metrologyConfiguration::addCustomPropertySet);
        }
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }

    @DELETE
    @Path("/{id}/custompropertysets/{cpsId}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public MetrologyConfigurationInfo removeCustomPropertySetFromMetrologyConfiguration(@PathParam("id") long id,
                                                                                        @PathParam("cpsId") String cpsId,
                                                                                        CustomPropertySetInfo<MetrologyConfigurationInfo> info) {
        if (info.parent != null) {
            info.parent.id = id;
        }
        RegisteredCustomPropertySet customPropertySet = resourceHelper.getRegisteredCustomPropertySetOrThrowException(cpsId);
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        if (metrologyConfiguration.getCustomPropertySets().contains(customPropertySet)) {
            metrologyConfiguration = resourceHelper.findAndLockCPSOnMetrologyConfiguration(info);
            metrologyConfiguration.removeCustomPropertySet(customPropertySet);
        }
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }
}
