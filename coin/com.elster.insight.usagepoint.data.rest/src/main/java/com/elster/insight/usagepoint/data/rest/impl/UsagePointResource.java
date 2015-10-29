package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Path("/usagepoints")
public class UsagePointResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;
    
    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService, TransactionService transactionService, Clock clock, Provider<ChannelResource> channelsOnUsagePointResourceProvider, Provider<RegisterResource> registersOnUsagePointResourceProvider, UsagePointConfigurationService usagePointConfigurationService) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.channelsOnUsagePointResourceProvider = channelsOnUsagePointResourceProvider;
        this.registersOnUsagePointResourceProvider = registersOnUsagePointResourceProvider;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @GET
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUsagePoints(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(true, params);
        
        for (UsagePoint up : list) {
            Optional<MetrologyConfiguration> mc = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(up);
            
        }
        
        
        List<UsagePointInfo> usagePointInfos = ListPager.of(list).from(queryParameters).stream().map(m -> new UsagePointInfo(m, clock, usagePointConfigurationService))
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
    @RolesAllowed({Privileges.Constants.ADMIN_OWN, Privileges.Constants.ADMIN_ANY})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response updateUsagePoint(@PathParam("id") String id, UsagePointInfo info, @Context SecurityContext securityContext) {
        transactionService.execute(new UpdateUsagePointTransaction(info, securityContext, meteringService, clock));
        return Response.status(Response.Status.CREATED).entity(getUsagePoint(info.mRID, securityContext)).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    @Path("/{mrid}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointInfo getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        UsagePointInfo result = new UsagePointInfo(usagePoint, clock, usagePointConfigurationService);
        result.addServiceLocationInfo();
        return result;
    }

    @POST
    @RolesAllowed({Privileges.Constants.ADMIN_ANY})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response createUsagePoint(UsagePointInfo info) {
        UsagePointInfo result = new UsagePointInfo(transactionService.execute(new CreateUsagePointTransaction(info, meteringService, clock)), clock, usagePointConfigurationService);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    @Path("/{mrid}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }


    @GET
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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

    private FluentIterable<? extends MeterActivation> meterActivationsForReadingTypeWithMRID(long id, String mRID, SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return FluentIterable.from(usagePoint.getMeterActivations()).filter(new HasReadingType(mRID));
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
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("MTR",Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal())
                && !((User) securityContext.getUserPrincipal()).hasPrivilege("MTR",Privileges.Constants.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

    private static class HasReadingType implements Predicate<MeterActivation> {
        private final MRIDMatcher mridMatcher;

        public HasReadingType(String mRID) {
            mridMatcher = new MRIDMatcher(mRID);
        }

        @Override
        public boolean apply(MeterActivation input) {
            return input != null && FluentIterable.from(input.getReadingTypes()).anyMatch(mridMatcher);
        }
    }

    private static class MRIDMatcher implements Predicate<ReadingType> {
        private final String mRID;

        private MRIDMatcher(String mRID) {
            this.mRID = mRID;
        }

        @Override
        public boolean apply(ReadingType input) {
            return input.getMRID().equals(mRID);
        }
    }
}