package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Path("/usagepoints")
public class UsagePointResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;

    @Inject
    public UsagePointResource(RestQueryService queryService, MeteringService meteringService, TransactionService transactionService, Clock clock) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
	@Produces(MediaType.APPLICATION_JSON) 
	public UsagePointInfos getUsagePoints(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(maySeeAny(securityContext), params);
        return toUsagePointInfos(params.clipToLimit(list), params.getStart(), params.getLimit());
	  }

    private UsagePointInfos toUsagePointInfos(List<UsagePoint> list, int start, int limit) {
        UsagePointInfos infos = new UsagePointInfos(list, clock);
        infos.addServiceLocationInfo();
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private List<UsagePoint> queryUsagePoints(boolean maySeeAny, QueryParameters queryParameters) {
        Query<UsagePoint> query = meteringService.getUsagePointQuery();
        query.setLazy("serviceLocation");
        RestQuery<UsagePoint> restQuery = queryService.wrap(query);
        List<UsagePoint> list;
        if (maySeeAny) {
            list = restQuery.select(queryParameters);
        } else {
            list = restQuery.select(queryParameters, meteringService.hasAccountability());
        }
        return list;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.BROWSE_ANY);
    }

    @PUT
	@RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos updateUsagePoint(@PathParam("id") long id, UsagePointInfo info, @Context SecurityContext securityContext) {
        info.id = id;
        transactionService.execute(new UpdateUsagePointTransaction(info, securityContext.getUserPrincipal(), meteringService, clock));
        return getUsagePoint(info.id, securityContext);
	}
	  
	@GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
	@Path("/{id}/")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos getUsagePoint(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);

        UsagePointInfos result = new UsagePointInfos(usagePoint, clock);
		result.addServiceLocationInfo();
		return result;
	}

	@POST
    @RolesAllowed({Privileges.ADMIN_ANY})
	@Consumes(MediaType.APPLICATION_JSON) 
	public UsagePointInfos createUsagePoint(UsagePointInfo info) {
		UsagePointInfos result = new UsagePointInfos();
        result.add(transactionService.execute(new CreateUsagePointTransaction(info, meteringService)), clock);
		return result;
	}

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/meteractivations")
    @Produces(MediaType.APPLICATION_JSON)
    public MeterActivationInfos getMeterActivations(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/meteractivations/{activationId}/channels")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingInfos getIntervalReadings(@PathParam("id") long id, @PathParam("activationId") long activationId, @PathParam("channelId") long channelId, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
    	if (from == 0 || to == 0) {
    		throw new WebApplicationException(Response.Status.BAD_REQUEST);
    	}
        return doGetIntervalreadings(id, activationId, channelId, securityContext, new Interval(new Date(from), new Date(to)));
    }

    private ReadingInfos doGetIntervalreadings(long id, long activationId, long channelId, SecurityContext securityContext, Interval interval) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        MeterActivation meterActivation = fetchMeterActivation(usagePoint, activationId);
        for (Channel channel : meterActivation.getChannels()) {
            if (channel.getId() == channelId) {
                List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(interval);
                return new ReadingInfos(intervalReadings);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/readingtypes")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfos getReadingTypes(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new ReadingTypeInfos(collectReadingTypes(usagePoint));
    }

    @GET
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfos getReadingTypes(@Context UriInfo uriInfo) {
        return new ReadingTypeInfos(meteringService.getAvailableReadingTypes());
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}/readingtypes/{mrid}/readings")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingInfos getReadingTypeReadings(@PathParam("id") long id, @PathParam("mrid") String mRID, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
        if (from == 0 || to == 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return doGetReadingTypeReadings(id, mRID, new Interval(new Date(from), new Date(to)), securityContext);
    }

    private ReadingInfos doGetReadingTypeReadings(long id, String mRID, Interval interval, SecurityContext securityContext) {
        ReadingType readingType = null;
        List<IntervalReadingRecord> readings = new ArrayList<>();
        for (MeterActivation meterActivation : meterActivationsForReadingTypeWithMRID(id, mRID, securityContext)) {
            if (readingType == null) {
                readingType = FluentIterable.from(meterActivation.getReadingTypes()).firstMatch(new MRIDMatcher(mRID)).get();
            }
            for (Channel channel : meterActivation.getChannels()) {
                readings.addAll(channel.getIntervalReadings(readingType, interval));
            }
        }
        return new ReadingInfos(readings);
    }

    private FluentIterable<MeterActivation> meterActivationsForReadingTypeWithMRID(long id, String mRID, SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return FluentIterable.from(usagePoint.getMeterActivations()).filter(new HasReadingType(mRID));
    }

    private Set<ReadingType> collectReadingTypes(UsagePoint usagePoint) {
        Set<ReadingType> readingTypes = new LinkedHashSet<>();
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (MeterActivation meterActivation : meterActivations) {
            readingTypes.addAll(meterActivation.getReadingTypes());
        }
        return readingTypes;
    }

    private UsagePoint fetchUsagePoint(long id, SecurityContext securityContext) {
        Optional<UsagePoint> found = meteringService.findUsagePoint(id);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        UsagePoint usagePoint = found.get();
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege(Privileges.BROWSE_ANY)) {
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
