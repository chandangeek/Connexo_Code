package com.elster.insight.usagepoint.config.rest.impl;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.Privileges;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final TransactionService transactionService;
    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final Clock clock;
    
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public MetrologyConfigurationResource(ResourceHelper resourceHelper, TransactionService transactionService, Clock clock, UsagePointConfigurationService usagePointConfigurationService, ValidationService validationService, CustomPropertySetService customPropertySetService, CustomPropertySetInfoFactory customPropertySetInfoFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.transactionService = transactionService;
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationService = validationService;
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getMeterologyConfigurations(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<MetrologyConfiguration> allMetrologyConfigurations = usagePointConfigurationService.findAllMetrologyConfigurations();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).stream().map(m -> new MetrologyConfigurationInfo(m))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }

    @GET
    //    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo getMeterologyConfiguration(@PathParam("id") long id, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return new MetrologyConfigurationInfo(metrologyConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response createMetrologyConfiguration(MetrologyConfigurationInfo metrologyConfigurationInfo) {
        MetrologyConfiguration metrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                return usagePointConfigurationService.newMetrologyConfiguration(metrologyConfigurationInfo.name);
            }
        });
        return Response.status(Response.Status.CREATED).entity(new MetrologyConfigurationInfo(metrologyConfiguration)).build();
    }

    @PUT
    //  @RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo metrologyConfigurationInfo, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        MetrologyConfiguration updatedMetrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                metrologyConfigurationInfo.writeTo(metrologyConfiguration);
                return metrologyConfiguration;
            }
        });

        return Response.status(Response.Status.CREATED).entity(new MetrologyConfigurationInfo(updatedMetrologyConfiguration)).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info) {
        info.id = id;
        transactionService.execute(() -> {
            MetrologyConfiguration mc = findAndLockMetrologyConfiguration(info);
            if (checkIfInUse(mc)) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
            mc.delete();
            return null;
        });
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    private Boolean checkIfInUse(MetrologyConfiguration mc) {
        return !usagePointConfigurationService.findUsagePointsForMetrologyConfiguration(mc).isEmpty();
        
    }

    private MetrologyConfiguration findAndLockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        return getLockedMetrologyConfiguration(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.id))
                        .supplier());
    }
    
    private Long getCurrentMetrologyConfigurationVersion(long id) {      
        Optional<MetrologyConfiguration> mc = usagePointConfigurationService.findMetrologyConfiguration(id);
        if (mc.isPresent())
            return mc.get().getVersion();
        return null;
    }

    private Optional<MetrologyConfiguration> getLockedMetrologyConfiguration(long id, long version) {
        return usagePointConfigurationService.findAndLockMetrologyConfiguration(id, version);
    }

    @GET
    //  @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/assignedvalidationrulesets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
            @Context SecurityContext securityContext,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<ValidationRuleSetInfo> validationRuleSetsInfos = ListPager.of(metrologyConfiguration.getValidationRuleSets()).from(queryParameters).stream().map(vrs -> new ValidationRuleSetInfo(vrs))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignedvalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @POST
    //  @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/assignedvalidationrulesets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response setAssignedValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
            ValidationRuleSetInfos validationRuleSetInfos,
            @Context SecurityContext securityContext,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        List<ValidationRuleSet> currentRules = metrologyConfiguration.getValidationRuleSets();

        transactionService.execute(new Transaction<Boolean>() {
            @Override
            public Boolean perform() {
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
                return true;
            }
        });

        return Response.status(Response.Status.CREATED).entity(new MetrologyConfigurationInfo(metrologyConfiguration)).build();
    }

    @GET
    //  @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/assignablevalidationrulesets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAssignableValidationRuleSetsForMetrologyConfiguration(@PathParam("id") long id,
            @Context SecurityContext securityContext,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam JsonQueryFilter filter) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<ValidationRuleSet> assigned = metrologyConfiguration.getValidationRuleSets();
        List<ValidationRuleSet> assignableValidationRuleSets = validationService.getValidationRuleSets().stream().filter(vrs -> !assigned.contains(vrs)).collect(Collectors.toList());

        List<ValidationRuleSetInfo> validationRuleSetsInfos = ListPager.of(assignableValidationRuleSets).from(queryParameters).stream().map(vrs -> new ValidationRuleSetInfo(vrs))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("assignablevalidationrulesets", validationRuleSetsInfos, queryParameters);
    }

    @GET
    @Path("/{id}/custompropertysets")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIG, Privileges.Constants.METROLOGY_CPS_VIEW})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigCustomPropertySets(@PathParam("id") long id,
                                                              @QueryParam("linked") @DefaultValue("true") boolean linked,
                                                              @BeanParam JsonQueryParameters queryParameters){
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
}