package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/usagepoints")
public class UsagePointResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;

    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;
    private final Provider<UsagePointValidationResource> usagePointValidationResourceProvider;
    private final Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService,
                              Clock clock,
                              Provider<ChannelResource> channelsOnUsagePointResourceProvider,
                              Provider<RegisterResource> registersOnUsagePointResourceProvider,
                              UsagePointConfigurationService usagePointConfigurationService,
                              Provider<UsagePointValidationResource> usagePointValidationResourceProvider,
                              Provider<UsagePointCustomPropertySetResource> usagePointCustomPropertySetResourceProvider) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.channelsOnUsagePointResourceProvider = channelsOnUsagePointResourceProvider;
        this.registersOnUsagePointResourceProvider = registersOnUsagePointResourceProvider;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointValidationResourceProvider = usagePointValidationResourceProvider;
        this.usagePointCustomPropertySetResourceProvider = usagePointCustomPropertySetResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public PagedInfoList getUsagePoints(@BeanParam JsonQueryParameters queryParameters,
                                        @BeanParam JsonQueryFilter filter,
                                        @Context UriInfo uriInfo) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(true, params);

        List<UsagePointInfo> usagePointInfos = ListPager.of(list)
                .from(queryParameters)
                .stream()
                .map(m -> new UsagePointInfo(m, clock, usagePointConfigurationService))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("usagePoints", usagePointInfos, queryParameters);
    }

    private List<UsagePoint> queryUsagePoints(boolean maySeeAny, QueryParameters queryParameters) {
        Query<UsagePoint> query = meteringService.getUsagePointQuery();
        query.setLazy("serviceLocation");
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        return queryService.wrap(query).select(queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMIN_OWN, Privileges.Constants.ADMIN_ANY})
    @Transactional
    public Response updateUsagePoint(@PathParam("id") String id, UsagePointInfo info, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = new UpdateUsagePointTransaction(info, securityContext, meteringService, clock).perform();

        MetrologyConfiguration metrologyConfiguration = null;
        if (info.metrologyConfiguration != null) {
            metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(info.metrologyConfiguration.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        }

        Optional<MetrologyConfiguration> currentMC = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(usagePoint);
        if (currentMC.isPresent() && info.metrologyConfiguration != null) {
            //check for update
            if (currentMC.get().getId() != info.metrologyConfiguration.id) {
                usagePointConfigurationService.link(usagePoint, metrologyConfiguration);
            }
        } else if (currentMC.isPresent() && info.metrologyConfiguration == null) {
            //unlink
            usagePointConfigurationService.unlink(usagePoint, metrologyConfiguration);
        } else {
            //new link
            if (info.metrologyConfiguration != null) {
                usagePointConfigurationService.link(usagePoint, metrologyConfiguration);
            }
        }
        return Response.status(Response.Status.CREATED).entity(getUsagePoint(info.mRID, securityContext)).build();
    }

    @GET
    @Path("/{mrid}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public UsagePointInfo getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        UsagePointInfo result = new UsagePointInfo(usagePoint, clock, usagePointConfigurationService);
        result.addServiceLocationInfo();
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMIN_ANY})
    @Transactional
    public Response createUsagePoint(UsagePointInfo info) {
        UsagePoint usagePoint = new CreateUsagePointTransaction(info, meteringService, clock).perform();
        if (info.metrologyConfiguration != null) {
            MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(info.metrologyConfiguration.id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
            usagePointConfigurationService.link(usagePoint, metrologyConfiguration);
        }
        UsagePointInfo result = new UsagePointInfo(usagePoint, clock, usagePointConfigurationService);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    @Path("/{mrid}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }


    @GET
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @Path("/{mrid}/channels")
    public ChannelResource getChannelResource() {
        return channelsOnUsagePointResourceProvider.get();
    }

    @Path("/{mrid}/registers")
    public RegisterResource getRegisterResource() {
        return registersOnUsagePointResourceProvider.get();
    }

    @Path("/{mrid}/validationrulesets")
    public UsagePointValidationResource getUsagePointValidationResource() {
        return usagePointValidationResourceProvider.get();
    }

    @Path("/{mrid}/properties")
    public UsagePointCustomPropertySetResource getUsagePointCustomPropertySetResource() {
        return usagePointCustomPropertySetResourceProvider.get();
    }


    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
        Set<ReadingType> readingTypes = new LinkedHashSet<>();
        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (MeterActivation meterActivation : meterActivations) {
            readingTypes.addAll(meterActivation.getReadingTypes());
        }
        return readingTypes;
    }

    private UsagePoint fetchUsagePoint(long id, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(id);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal())
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("INS", Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal())
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("INS", Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }
}