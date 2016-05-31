package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.transaction.TransactionService;
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
import java.util.Set;
import java.util.stream.Collectors;


@Path("/usagepoints")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointInfoFactory usagePointInfoFactory;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointResource(MeteringService meteringService,
                              ServiceCallService serviceCallService,
                              TransactionService transactionService,
                              Clock clock,
                              ConcurrentModificationExceptionFactory conflictFactory,
                              UsagePointInfoFactory usagePointInfoFactory,
                              ExceptionFactory exceptionFactory,
                              Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.conflictFactory = conflictFactory;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
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
        return securityContext.isUserInRole(Privileges.Constants.VIEW_ANY_USAGEPOINT);
    }


    @PUT
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Transactional
    public UsagePointInfo updateUsagePoint(@PathParam("mRID") String mRID, UsagePointInfo info) {
        UsagePoint usagePoint = meteringService.findAndLockUsagePointByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.mRID)
                        .withActualVersion(() -> meteringService.findUsagePoint(mRID).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
        info.writeTo(usagePoint);
        return usagePointInfoFactory.from(usagePoint);
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Path("/{mRID}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public UsagePointInfo getUsagePoint(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return new UsagePointInfo(usagePoint, clock);
    }

    @POST
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response createUsagePoint(UsagePointInfo info) {
        new RestValidationBuilder()
                .notEmpty(info.mRID, "mRID")
                .notEmpty(info.serviceCategory, "serviceCategory")
                .validate();
        if (info.installationTime == null) {
            info.installationTime = clock.instant().toEpochMilli();
        }
        UsagePoint usagePoint = usagePointInfoFactory.newUsagePointBuilder(info).create();
        usagePoint.addDetail(usagePoint.getServiceCategory()
                .newUsagePointDetail(usagePoint, clock.instant()));
        usagePoint.update();
        return Response.status(Response.Status.CREATED).entity(usagePointInfoFactory.from(usagePoint)).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public MeterActivationInfos getMeterActivations(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/meteractivations/{activationId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ChannelInfos getChannels(@PathParam("mRID") String mRID, @PathParam("activationId") long activationId, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/meteractivations/{activationId}/channels/{channelId}/intervalreadings")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingInfos getIntervalReadings(@PathParam("mRID") String mRID, @PathParam("activationId") long activationId, @PathParam("channelId") long channelId, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
        if (from == 0 || to == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Range<Instant> range = Range.openClosed(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
        return doGetIntervalreadings(mRID, activationId, channelId, securityContext, range);
    }

    private ReadingInfos doGetIntervalreadings(String mRID, long activationId, long channelId, SecurityContext securityContext, Range<Instant> range) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @GET
    @Path("/readingtypes")
    @RolesAllowed({Privileges.Constants.VIEW_READINGTYPE, Privileges.Constants.ADMINISTER_READINGTYPE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@Context UriInfo uriInfo) {
        return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/readingtypes/{rtMrid}/readings")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingInfos getReadingTypeReadings(@PathParam("mRID") String mRID, @PathParam("rtMrid") String rtMrid, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
        if (from == 0 || to == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Range<Instant> range = Range.openClosed(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
        return doGetReadingTypeReadings(mRID, rtMrid, range);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Path("/{mRID}/servicecalls")
    public Response cancelServiceCallsFor(@PathParam("mRID") String mRID, ServiceCallInfo serviceCallInfo, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        if (serviceCallInfo.state == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (DefaultState.CANCELLED.getKey().equals(serviceCallInfo.state.id)) {
            serviceCallService.cancelServiceCallsFor(usagePoint);
            return Response.accepted().build();
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    private ReadingInfos doGetReadingTypeReadings(String mRID, String rtMrid, Range<Instant> range) {
        ReadingType readingType = null;
        List<IntervalReadingRecord> readings = new ArrayList<>();
        for (MeterActivation meterActivation : meterActivationsForReadingTypeWithMRID(mRID, rtMrid)) {
            if (readingType == null) {
                readingType = FluentIterable.from(meterActivation.getReadingTypes()).firstMatch(new MRIDMatcher(rtMrid)).get();
            }
            for (Channel channel : meterActivation.getChannels()) {
                readings.addAll(channel.getIntervalReadings(readingType, range));
            }
        }
        return new ReadingInfos(readings);
    }

    private FluentIterable<? extends MeterActivation> meterActivationsForReadingTypeWithMRID(String mRID, String rtMrid) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return FluentIterable.from(usagePoint.getMeterActivations()).filter(new HasReadingType(rtMrid));
    }

    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
        Set<ReadingType> readingTypes = new LinkedHashSet<>();
        List<? extends MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (MeterActivation meterActivation : meterActivations) {
            readingTypes.addAll(meterActivation.getReadingTypes());
        }
        return readingTypes;
    }

    private UsagePoint fetchUsagePoint(long id) {
        return meteringService.findUsagePoint(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private UsagePoint fetchUsagePoint(String mRID) {
        return meteringService.findUsagePoint(mRID)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mRID));
    }

    private static class HasReadingType implements Predicate<MeterActivation> {

        private final MRIDMatcher mridMatcher;

        private HasReadingType(String mRID) {
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
