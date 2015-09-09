package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/usagepoints")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;

    @Inject
    public UsagePointResource(MeteringService meteringService, TransactionService transactionService, Clock clock) {
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getUsagePoints(@Context SecurityContext securityContext,
                                          @BeanParam JsonQueryParameters queryParameters,
                                          @QueryParam("like") String like) {
        UsagePointFilter usagePointFilter = new UsagePointFilter();
        if (!Checks.is(like).emptyOrOnlyWhiteSpace()){
            usagePointFilter.setMrid("*"+like+"*");
        }
        usagePointFilter.setAccountabilityOnly(!maySeeAny(securityContext));
        List<UsagePointInfo> usagePoints = meteringService.getUsagePoints(usagePointFilter).from(queryParameters)
                .stream()
                .map(usagePoint -> new UsagePointInfo(usagePoint, clock))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("usagePoints", usagePoints, queryParameters);
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.BROWSE_ANY);
    }

    @PUT
    @RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
    @Path("/{mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointInfos updateUsagePoint(@PathParam("mrid") String mRid, UsagePointInfo info, @Context SecurityContext securityContext) {
        info.mRID = mRid;
        transactionService.execute(new UpdateUsagePointTransaction(info, securityContext, meteringService, clock));
        return getUsagePoint(info.mRID, securityContext);
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{mrid}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointInfos getUsagePoint(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        UsagePointInfos result = new UsagePointInfos(usagePoint, clock);
        result.addServiceLocationInfo();
        return result;
    }

    @POST
    @RolesAllowed({Privileges.ADMIN_ANY})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public UsagePointInfos createUsagePoint(UsagePointInfo info) {
        UsagePointInfos result = new UsagePointInfos();
        result.add(transactionService.execute(new CreateUsagePointTransaction(info, meteringService)), clock);
        return result;
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{mrid}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterActivationInfos getMeterActivations(@PathParam("mrid") String mRid, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRid, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/meteractivations/{activationId}/channels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ChannelInfos getChannels(@PathParam("id") long id, @PathParam("activationId") long activationId, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        MeterActivation meterActivation = fetchMeterActivation(usagePoint, activationId);
        return new ChannelInfos(meterActivation.getChannels());
    }

    private MeterActivation fetchMeterActivation(UsagePoint usagePoint, long activationId) {
        for (MeterActivation meterActivation : usagePoint.getMeterActivations()) {
            if (meterActivation.getId() == activationId) {
                return meterActivation;
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/meteractivations/{activationId}/channels/{channelId}/intervalreadings")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingInfos getIntervalReadings(@PathParam("id") long id, @PathParam("activationId") long activationId, @PathParam("channelId") long channelId, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
        if (from == 0 || to == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Range<Instant> range = Range.openClosed(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
        return doGetIntervalreadings(id, activationId, channelId, securityContext, range);
    }

    private ReadingInfos doGetIntervalreadings(long id, long activationId, long channelId, SecurityContext securityContext, Range<Instant> range) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        MeterActivation meterActivation = fetchMeterActivation(usagePoint, activationId);
        for (Channel channel : meterActivation.getChannels()) {
            if (channel.getId() == channelId) {
                List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(range);
                return new ReadingInfos(intervalReadings);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @GET
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@Context UriInfo uriInfo) {
        return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/readingtypes/{mrid}/readings")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ReadingInfos getReadingTypeReadings(@PathParam("id") long id, @PathParam("mrid") String mRID, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
        if (from == 0 || to == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Range<Instant> range = Range.openClosed(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
        return doGetReadingTypeReadings(id, mRID, range, securityContext);
    }

    private ReadingInfos doGetReadingTypeReadings(long id, String mRID, Range<Instant> range, SecurityContext securityContext) {
        ReadingType readingType = null;
        List<IntervalReadingRecord> readings = new ArrayList<>();
        for (MeterActivation meterActivation : meterActivationsForReadingTypeWithMRID(id, mRID, securityContext)) {
            if (readingType == null) {
                readingType = FluentIterable.from(meterActivation.getReadingTypes()).firstMatch(new MRIDMatcher(mRID)).get();
            }
            for (Channel channel : meterActivation.getChannels()) {
                readings.addAll(channel.getIntervalReadings(readingType, range));
            }
        }
        return new ReadingInfos(readings);
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
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege("MDC",Privileges.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege("MDC",Privileges.BROWSE_ANY)) {
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