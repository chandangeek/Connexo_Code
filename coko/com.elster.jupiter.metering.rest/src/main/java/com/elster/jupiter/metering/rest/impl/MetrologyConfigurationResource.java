package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public MetrologyConfigurationResource(MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory,
                                          MetrologyConfigurationService metrologyConfigurationService,
                                          MeteringService meteringService,
                                          ExceptionFactory exceptionFactory,
                                          ResourceHelper resourceHelper) {
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
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
                .map(metrologyConfigurationInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyConfigurations", metrologyConfigurationsInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_METROLOGY_CONFIGURATION, Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo getMetrologyConfiguration(@PathParam("id") long id) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findMetrologyConfiguration(id);
        return metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response createMetrologyConfiguration(MetrologyConfigurationInfo info) {
        new RestValidationBuilder()
                .notEmpty(info.name, "name")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .notEmpty(info.readingTypes, "readingTypes")
                .validate();

        ServiceCategory serviceCategory = meteringService.getServiceCategory(Arrays.stream(ServiceKind.values())
                .filter(kind -> kind.name().equals(info.serviceCategory.id))
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.SERVICE_CATEGORY_NOT_FOUND)))
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.SERVICE_CATEGORY_NOT_FOUND));
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.newUsagePointMetrologyConfiguration(info.name, serviceCategory)
                .withDescription(info.description).create();

        MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEFAULT_METROLOGY_PURPOSE_NOT_FOUND));
        MeterRole meterRoleDefault = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEFAULT_METER_ROLE_NOT_FOUND));

        metrologyConfiguration.addMeterRole(meterRoleDefault);

        info.readingTypes.stream().forEach(readingTypeInfo -> {
            ReadingType readingType = meteringService.findReadingTypes(Collections.singletonList(readingTypeInfo.mRID)).stream().findFirst()
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.READING_TYPE_NOT_FOUND));
            FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = metrologyConfiguration.newReadingTypeRequirement(readingType.getFullAliasName())
                    .withMeterRole(meterRoleDefault)
                    .withReadingType(readingType);
            ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.AUTO);
            ReadingTypeDeliverable deliverable = builder.build(builder.requirement(fullySpecifiedReadingTypeRequirement));
            MetrologyContract metrologyContract = metrologyConfiguration.addMetrologyContract(purpose);
            metrologyContract.addDeliverable(deliverable);
        });

        return Response.status(Response.Status.CREATED).entity(metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Transactional
    public Response updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo info, @Context SecurityContext securityContext) {
        UsagePointMetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);

        metrologyConfiguration.startUpdate()
                .setName(info.name)
                .setDescription(info.description)
                .complete();

        return Response.ok().entity(metrologyConfigurationInfoFactory.asInfo(metrologyConfiguration)).build();
    }

    @DELETE
    @RolesAllowed({Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response deleteMetrologyConfiguration(@PathParam("id") String id, MetrologyConfigurationInfo info) {
        MetrologyConfiguration metrologyConfiguration = resourceHelper.findAndLockMetrologyConfiguration(info);

        if (metrologyConfiguration.isActive()) {
            throw exceptionFactory.newException(MessageSeeds.YOU_CANNOT_REMOVE_ACTIVE_METROLOGY_CONFIGURATION);
        }

        metrologyConfiguration.delete();

        return Response.status(Response.Status.OK).build();
    }
}