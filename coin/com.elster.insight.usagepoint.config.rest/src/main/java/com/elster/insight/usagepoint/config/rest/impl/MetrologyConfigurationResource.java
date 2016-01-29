package com.elster.insight.usagepoint.config.rest.impl;

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.Privileges;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
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
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private final Clock clock;

    @Inject
    public MetrologyConfigurationResource(ResourceHelper resourceHelper, Clock clock, UsagePointConfigurationService usagePointConfigurationService, ValidationService validationService, CustomPropertySetService customPropertySetService, CustomPropertySetInfoFactory customPropertySetInfoFactory, MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationService = validationService;
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getMeterologyConfigurations(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<MetrologyConfiguration> allMetrologyConfigurations = usagePointConfigurationService.findAllMetrologyConfigurations();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations)
                .from(queryParameters)
                .stream()
                .map(metrologyConfigurationInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo getMeterologyConfiguration(@PathParam("id") long id) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG, Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG})
    @Transactional
    public Response createMetrologyConfiguration(MetrologyConfigurationInfo metrologyConfigurationInfo) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.newMetrologyConfiguration(metrologyConfigurationInfo.name);
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG, Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG})
    @Transactional
    public Response updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        info.writeTo(metrologyConfiguration);
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}/activate")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo activateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        metrologyConfiguration.activate();
        return metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration);
    }

    @PUT
    @Path("/{id}/deactivate")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo deactivateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        metrologyConfiguration.deactivate();
        return metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
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

    private Boolean checkIfInUse(MetrologyConfiguration mc) {
        return !usagePointConfigurationService.findUsagePointsForMetrologyConfiguration(mc).isEmpty();

    }

    @GET
    @Path("/{id}/assignedvalidationrulesets")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                                @Context SecurityContext securityContext,
                                                                                @BeanParam JsonQueryParameters queryParameters,
                                                                                @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<ValidationRuleSetInfo> validationRuleSetsInfos = ListPager.of(metrologyConfiguration.getValidationRuleSets())
                .from(queryParameters)
                .stream()
                .map(ValidationRuleSetInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignedvalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @POST
    @Path("/{id}/assignedvalidationrulesets")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response setAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                           ValidationRuleSetInfos validationRuleSetInfos,
                                                                           @BeanParam JsonQueryParameters queryParameters,
                                                                           @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        List<ValidationRuleSet> currentRules = metrologyConfiguration.getValidationRuleSets();
        for (ValidationRuleSetInfo vrsi : validationRuleSetInfos.ruleSets) {
            ValidationRuleSet vrs = validationService.getValidationRuleSet(vrsi.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            if (currentRules.contains(vrs)) {
                currentRules.remove(vrs);
            } else {
                metrologyConfiguration.addValidationRuleSet(vrs);
            }
        }

        //remove rules that are no longer current
        for (ValidationRuleSet vrs : currentRules) {
            metrologyConfiguration.removeValidationRuleSet(vrs);
        }
        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration)).build();
    }

    @GET
    @Path("/{id}/assignablevalidationrulesets")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignableValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
                                                                                  @BeanParam JsonQueryParameters queryParameters,
                                                                                  @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.getMetrologyConfigOrThrowException(id);
        List<ValidationRuleSet> assigned = metrologyConfiguration.getValidationRuleSets();
        List<ValidationRuleSet> assignableValidationRuleSets = validationService.getValidationRuleSets().stream().filter(vrs -> !assigned.contains(vrs)).collect(Collectors.toList());

        List<ValidationRuleSetInfo> validationRuleSetsInfos = ListPager.of(assignableValidationRuleSets)
                .from(queryParameters)
                .stream()
                .map(ValidationRuleSetInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignablevalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @GET
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG, Privileges.Constants.METROLOGY_CPS_VIEW})
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
            customPropertySets = customPropertySetService.findActiveCustomPropertySets(MetrologyConfiguration.class)
                    .stream()
                    .filter(cps -> !assignedCPSIds.contains(cps.getCustomPropertySet().getId()));
        }
        List<?> infos = customPropertySets
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(customPropertySetInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }

    @PUT
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.METROLOGY_CPS_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo addCustomPropertySetToMetrologyConfiguration(@PathParam("id") long id,
                                                                                   @BeanParam JsonQueryParameters queryParameters,
                                                                                   MetrologyConfigurationInfo info) {
        info.id = id;
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        if (info.customPropertySets != null) {
            info.customPropertySets
                    .stream()
                    .map(cpsInfo -> resourceHelper.getRegisteredCustomPropertySetOrThrowException(cpsInfo.cpsId))
                    .forEach(metrologyConfiguration::addCustomPropertySet);
        }
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }

    @DELETE
    @Path("/{id}/custompropertysets/{cpsId}")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY_METROLOGY_CONFIG, Privileges.Constants.METROLOGY_CPS_ADMIN})
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public MetrologyConfigurationInfo removeCustomPropertySetFromMetrologyConfiguration(@PathParam("id") long id,
                                                                                        @PathParam("cpsId") String cpsId,
                                                                                        @BeanParam JsonQueryParameters queryParameters,
                                                                                        MetrologyConfigurationInfo info) {
        info.id = id;
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);
        RegisteredCustomPropertySet customPropertySet = resourceHelper.getRegisteredCustomPropertySetOrThrowException(cpsId);
        metrologyConfiguration.removeCustomPropertySet(customPropertySet);
        return metrologyConfigurationInfoFactory.asDetailedInfo(metrologyConfiguration);
    }
}