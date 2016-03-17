package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Checks;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Path("/usagepoints")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointInfoFactory usagePointInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointResource(MeteringService meteringService,
                              TransactionService transactionService,
                              Clock clock,
                              ConcurrentModificationExceptionFactory conflictFactory,
                              UsagePointInfoFactory usagePointInfoFactory,
                              Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
        this.conflictFactory = conflictFactory;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.thesaurus = thesaurus;
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
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> meteringService.findUsagePoint(mRID).map(UsagePoint::getVersion).orElse(Long.valueOf(0)))
                        .supplier());
        info.writeTo(usagePoint);
        return usagePointInfoFactory.from(usagePoint);
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT,
            Privileges.Constants.ADMINISTER_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public UsagePointInfo getUsagePoint(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        UsagePointInfo result = new UsagePointInfo(usagePoint, clock);
        return result;
    }

    @DELETE
    @RolesAllowed({Privileges.Constants.ADMINISTER_ANY_USAGEPOINT})
    @Path("/{mRID}")
    @Transactional
    public Response deleteUsagePoint(@PathParam("mRID") String mRID) {
        Optional<UsagePoint> usagePoint = meteringService.findUsagePoint(mRID);
        if (usagePoint.isPresent()) {
            usagePoint.get().delete();
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
    @Path("/{id}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public MeterActivationInfos getMeterActivations(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{id}/meteractivations/{activationId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{id}/meteractivations/{activationId}/channels/{channelId}/intervalreadings")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
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
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
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
    @Path("/{id}/readingtypes/{mrid}/readings")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
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
        return meteringService.findUsagePoint(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private UsagePoint fetchUsagePoint(String mRid, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(mRid);
        UsagePoint usagePoint = found.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal())
                .hasPrivilege("MTR", Privileges.Constants.VIEW_ANY_USAGEPOINT)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
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
