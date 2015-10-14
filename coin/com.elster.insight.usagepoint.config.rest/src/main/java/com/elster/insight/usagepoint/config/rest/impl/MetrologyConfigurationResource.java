package com.elster.insight.usagepoint.config.rest.impl;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
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

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final TransactionService transactionService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;

    @Inject
    public MetrologyConfigurationResource(TransactionService transactionService, Clock clock, UsagePointConfigurationService usagePointConfigurationService) {
        this.transactionService = transactionService;
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getMeterologyConfigurations(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<MetrologyConfiguration> allMetrologyConfigurations = usagePointConfigurationService.findAllMetrologyConfigurations().find();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).stream().map(m -> new MetrologyConfigurationInfo(m))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public MetrologyConfigurationInfo createMetrologyConfiguration(MetrologyConfigurationInfo metrologyConfigurationInfo) {
        MetrologyConfiguration metrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                return usagePointConfigurationService.newMetrologyConfiguration(metrologyConfigurationInfo.name);
            }
        });
        return new MetrologyConfigurationInfo(metrologyConfiguration);
    }

    @PUT
    //  @RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo metrologyConfigurationInfo, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        
        MetrologyConfiguration updatedMetrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                metrologyConfigurationInfo.writeTo(metrologyConfiguration);
                metrologyConfiguration.save();
                return metrologyConfiguration;
            }
        });
        
        
        return new MetrologyConfigurationInfo(updatedMetrologyConfiguration);
    }
    
//    @PUT
//    @Path("/{deviceConfigurationId}")
//    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
//    public DeviceConfigurationInfo updateDeviceConfigurations(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, DeviceConfigurationInfo deviceConfigurationInfo) {
//        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
//        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
//        deviceConfigurationInfo.writeTo(deviceConfiguration);
//        deviceConfiguration.save();
//        return new DeviceConfigurationInfo(deviceConfiguration);
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