package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;

    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigurationResource(ResourceHelper resourceHelper, MeteringService meteringService, UsagePointConfigurationService usagePointConfigurationService, ValidationService validationService, CustomPropertySetService customPropertySetService, CustomPropertySetInfoFactory customPropertySetInfoFactory, MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory, MetrologyConfigurationService metrologyConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationService = validationService;
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointMetrologyConfiguration> allMetrologyConfigurations = metrologyConfigurationService.findAllUsagePointMetrologyConfigurations();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).find()
                .stream()
                .map(metrologyConfigurationInfoFactory::asInfo)
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
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration(metrologyConfigurationInfo.name, serviceCategory).create();
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info, @Context SecurityContext securityContext) {
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

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response deleteMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        MetrologyConfiguration mc = resourceHelper.findAndLockMetrologyConfiguration(info);
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
    @Path("/{id}/assignedvalidationrulesets")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                                @Context SecurityContext securityContext,
                                                                                @BeanParam JsonQueryParameters queryParameters,
                                                                                @BeanParam JsonQueryFilter filter) {
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findUsagePointMetrologyConfiguration(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<ValidationRuleSetInfo> validationRuleSetsInfos =
                ListPager
                        .of(usagePointConfigurationService.getValidationRuleSets(metrologyConfiguration))
                        .from(queryParameters)
                        .find()
                        .stream()
                        .map(ValidationRuleSetInfo::new)
                        .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignedvalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @POST
    @Path("/{id}/assignedvalidationrulesets")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response setAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                           ValidationRuleSetInfos validationRuleSetInfos,
                                                                           @BeanParam JsonQueryParameters queryParameters,
                                                                           @BeanParam JsonQueryFilter filter) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        List<ValidationRuleSet> currentRules = usagePointConfigurationService.getValidationRuleSets(metrologyConfiguration);
        for (ValidationRuleSetInfo vrsi : validationRuleSetInfos.ruleSets) {
            ValidationRuleSet validationRuleSet = validationService.getValidationRuleSet(vrsi.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            if (currentRules.contains(validationRuleSet)) {
                currentRules.remove(validationRuleSet);
            } else {
                usagePointConfigurationService.addValidationRuleSet(metrologyConfiguration, validationRuleSet);
            }
        }

        //remove rules that are no longer current
        for (ValidationRuleSet currentRule : currentRules) {
            usagePointConfigurationService.removeValidationRuleSet(metrologyConfiguration, currentRule);
        }
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @GET
    @Path("/{id}/assignablevalidationrulesets")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignableValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                                  @BeanParam JsonQueryParameters queryParameters,
                                                                                  @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        List<ValidationRuleSet> assigned = usagePointConfigurationService.getValidationRuleSets(metrologyConfiguration);
        List<ValidationRuleSet> assignableValidationRuleSets = validationService.getValidationRuleSets().stream().filter(vrs -> !assigned.contains(vrs)).collect(Collectors.toList());

        List<ValidationRuleSetInfo> validationRuleSetsInfos = ListPager.of(assignableValidationRuleSets)
                .from(queryParameters)
                .find()
                .stream()
                .map(ValidationRuleSetInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignablevalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @GET
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigCustomPropertySets(@PathParam("id") long id,
                                                              @QueryParam("linked") @DefaultValue("true") boolean linked,
                                                              @BeanParam JsonQueryParameters queryParameters) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        Stream<RegisteredCustomPropertySet> customPropertySets = metrologyConfiguration.getCustomPropertySets().stream();
        if (!linked) {
            Set<String> assignedCPSIds = customPropertySets
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .map(CustomPropertySet::getId)
                    .collect(Collectors.toSet());
            customPropertySets = customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)
                    .stream()
                    .filter(cps -> !assignedCPSIds.contains(cps.getCustomPropertySet().getId()));
        }
        List<?> infos = customPropertySets
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
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
                                                                                   @BeanParam JsonQueryParameters queryParameters,
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
                                                                                        @BeanParam JsonQueryParameters queryParameters,
                                                                                        CustomPropertySetInfo<MetrologyConfigurationInfo> info) {
        if (info.parent != null){
            info.parent.id = id;
        }
        RegisteredCustomPropertySet customPropertySet = resourceHelper.getRegisteredCustomPropertySetOrThrowException(cpsId);
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        if (metrologyConfiguration.getCustomPropertySets().contains(customPropertySet)){
            metrologyConfiguration = resourceHelper.findAndLockCPSOnMetrologyConfiguration(info);
            metrologyConfiguration.removeCustomPropertySet(customPropertySet);
        }
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }
}