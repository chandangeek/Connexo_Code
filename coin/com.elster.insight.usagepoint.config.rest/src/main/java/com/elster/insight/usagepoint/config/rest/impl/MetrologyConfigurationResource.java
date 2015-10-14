package com.elster.insight.usagepoint.config.rest.impl;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

//    private final RestQueryService queryService;
//    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;
    
//    private final Provider<ChannelResource> channelsOnUsagePointResourceProvider;
//    private final Provider<RegisterResource> registersOnUsagePointResourceProvider;

    @Inject
    public MetrologyConfigurationResource(TransactionService transactionService, Clock clock, UsagePointConfigurationService usagePointConfigurationService) {
//        this.queryService = queryService;
//        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
//        this.channelsOnUsagePointResourceProvider = channelsOnUsagePointResourceProvider;
//        this.registersOnUsagePointResourceProvider = registersOnUsagePointResourceProvider;
    }

//    @GET
//    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public Response getUsagePoints(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
//        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
//        List<UsagePoint> list = queryUsagePoints(maySeeAny(securityContext), params);
//        return toUsagePointInfos(params.clipToLimit(list), params.getStartInt(), params.getLimit());
//    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getMeterologyConfigurations(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<MetrologyConfiguration> allMetrologyConfigurations = usagePointConfigurationService.findAllMetrologyConfigurations().find();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).stream().map(MetrologyConfigurationInfo::from).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }

//    private List<UsagePoint> queryUsagePoints(boolean maySeeAny, QueryParameters queryParameters) {
//        Query<UsagePoint> query = meteringService.getUsagePointQuery();
//        query.setLazy("serviceLocation");
//        if (!maySeeAny) {
//            query.setRestriction(meteringService.hasAccountability());
//        }
//        return queryService.wrap(query).select(queryParameters);
//    }
//
//    private boolean maySeeAny(SecurityContext securityContext) {
//        return securityContext.isUserInRole(Privileges.BROWSE_ANY);
//    }
//
//    @PUT
//    @RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public UsagePointInfos updateUsagePoint(@PathParam("id") String id, UsagePointInfo info, @Context SecurityContext securityContext) {
//        transactionService.execute(new UpdateUsagePointTransaction(info, securityContext, meteringService, clock));
//        return getUsagePoint(info.mRID, securityContext);
//    }
//
//    @GET
//    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
//    @Path("/{mrid}/")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public UsagePointInfos getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
//        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
//        UsagePointInfos result = new UsagePointInfos(usagePoint, clock);
//        result.addServiceLocationInfo();
//        return result;
//    }
//
//    @POST
//    @RolesAllowed({Privileges.ADMIN_ANY})
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public UsagePointInfos createUsagePoint(UsagePointInfo info) {
//        UsagePointInfos result = new UsagePointInfos();
//        result.add(transactionService.execute(new CreateUsagePointTransaction(info, meteringService, clock)), clock);
//        return result;
//    }
//
//    @GET
//    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
//    @Path("/{mrid}/meteractivations")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
//        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
//        return new MeterActivationInfos(usagePoint.getMeterActivations());
//    }
//
//
//    @GET
//    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
//    @Path("/{id}/readingtypes")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
//        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
//        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
//    }
//
////    @GET
////    @Path("/readingtypes")
////    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
////    public ReadingTypeInfos getReadingTypes(@Context UriInfo uriInfo) {
////        return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
////    }
//    
//    @Path("/{mrid}/channels")
//    public ChannelResource getChannelResource() {
//        return channelsOnUsagePointResourceProvider.get();
//    }
//    
//    @Path("/{mrid}/registers")
//    public RegisterResource getRegisterResource() {
//        return registersOnUsagePointResourceProvider.get();
//    }
//
//    private FluentIterable<? extends MeterActivation> meterActivationsForReadingTypeWithMRID(long id, String mRID, SecurityContext securityContext) {
//        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
//        return FluentIterable.from(usagePoint.getMeterActivations()).filter(new HasReadingType(mRID));
//    }
//
//    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
//        Set<ReadingType> readingTypes = new LinkedHashSet<>();
//        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
//        for (MeterActivation meterActivation : meterActivations) {
//            readingTypes.addAll(meterActivation.getReadingTypes());
//        }
//        return readingTypes;
//    }
//
//    private UsagePoint fetchUsagePoint(long id, SecurityContext securityContext) {
//        Optional<UsagePoint> found = meteringService.findUsagePoint(id);
//        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
//        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege("MTR",Privileges.BROWSE_ANY)) {
//            throw new WebApplicationException(Response.Status.FORBIDDEN);
//        }
//        return usagePoint;
//    }
//
//    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
//        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
//        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
//        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege("MTR",Privileges.BROWSE_ANY)) {
//            throw new WebApplicationException(Response.Status.FORBIDDEN);
//        }
//        return usagePoint;
//    }
//
//    private static class HasReadingType implements Predicate<MeterActivation> {
//        private final MRIDMatcher mridMatcher;
//
//        public HasReadingType(String mRID) {
//            mridMatcher = new MRIDMatcher(mRID);
//        }
//
//        @Override
//        public boolean apply(MeterActivation input) {
//            return input != null && FluentIterable.from(input.getReadingTypes()).anyMatch(mridMatcher);
//        }
//    }
//
//    private static class MRIDMatcher implements Predicate<ReadingType> {
//        private final String mRID;
//
//        private MRIDMatcher(String mRID) {
//            this.mRID = mRID;
//        }
//
//        @Override
//        public boolean apply(ReadingType input) {
//            return input.getMRID().equals(mRID);
//        }
//    }
}