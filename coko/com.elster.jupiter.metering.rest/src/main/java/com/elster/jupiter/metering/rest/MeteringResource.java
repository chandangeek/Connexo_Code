package com.elster.jupiter.metering.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.annotation.security.RolesAllowed;
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
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.metering.rest.Bus.getMeteringService;
import static com.elster.jupiter.metering.rest.Bus.getQueryService;

@Path("/mtr")
public class MeteringResource {

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
	@Path("/usagepoints")
	@Produces(MediaType.APPLICATION_JSON) 
	public UsagePointInfos getUsagePoints(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePoint> list = queryUsagePoints(maySeeAny(securityContext), params);
        return toUsagePointInfos(list, params.getStart(), params.getLimit());
	  }

    private UsagePointInfos toUsagePointInfos(List<UsagePoint> list, int start, int limit) {
        UsagePointInfos infos = new UsagePointInfos(list);
        infos.addServiceLocationInfo();
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private List<UsagePoint> queryUsagePoints(boolean maySeeAny, QueryParameters queryParameters) {
        Query<UsagePoint> query = getMeteringService().getUsagePointQuery();
        query.setLazy("serviceLocation");
        RestQuery<UsagePoint> restQuery = getQueryService().wrap(query);
        List<UsagePoint> list;
        if (maySeeAny) {
            list = restQuery.select(queryParameters);
        } else {
            list = restQuery.select(queryParameters, Bus.getMeteringService().hasAccountability());
        }
        return list;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.BROWSE_ANY);
    }

    @PUT
	@RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
	@Path("/usagepoints/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos updateUsagePoint(@PathParam("id") long id, UsagePointInfo info, @Context SecurityContext securityContext) {
        info.id = id;
        Bus.getServiceLocator().getTransactionService().execute(new UpdateUsagePointTransaction(info, securityContext.getUserPrincipal()));
        return getUsagePoint(info.id, securityContext);
	}
	  
	@GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
	@Path("/usagepoints/{id}/")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos getUsagePoint(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);

        UsagePointInfos result = new UsagePointInfos(usagePoint);
		result.addServiceLocationInfo();
		return result;
	}

	@POST
    @RolesAllowed({Privileges.ADMIN_ANY})
	@Path("/usagepoints")
	@Consumes(MediaType.APPLICATION_JSON) 
	public UsagePointInfos createUsagePoint(UsagePointInfo info) {
		UsagePointInfos result = new UsagePointInfos();
        result.add(Bus.getTransactionService().execute(new CreateUsagePointTransaction(info)));
		return result;
	}

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/usagepoints/{id}/meterActivations")
    @Produces(MediaType.APPLICATION_JSON)
    public MeterActivationInfos getMeterActivations(@PathParam("id") long id, @Context SecurityContext securityContext) {
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        return new MeterActivationInfos(usagePoint.getMeterActivations());
    }

    @GET
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/usagepoints/{id}/meterActivations/{activationId}/channels")
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
    @Path("/usagepoints/{id}/meterActivations/{activationId}/channels/{channelId}/intervalreadings")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingInfos getIntervalReadings(@PathParam("id") long id, @PathParam("activationId") long activationId, @PathParam("channelId") long channelId, @QueryParam("from") long from, @QueryParam("to") long to, @Context SecurityContext securityContext) {
    	if (from == 0 || to == 0) {
    		throw new WebApplicationException(Response.Status.BAD_REQUEST);
    	}
        UsagePoint usagePoint = fetchUsagePoint(id, securityContext);
        MeterActivation meterActivation = fetchMeterActivation(usagePoint, activationId);
        for (Channel channel : meterActivation.getChannels()) {
            if (channel.getId() == channelId) {
                List<IntervalReading> intervalReadings = fetchIntervalReadings(channel, from, to);
                return new ReadingInfos(intervalReadings);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private List<IntervalReading> fetchIntervalReadings(Channel channel, long from, long to) {
        return channel.getIntervalReadings(new Date(from), new Date(to));
    }

    private UsagePoint fetchUsagePoint(long id, SecurityContext securityContext) {
        Optional<UsagePoint> found = Bus.getMeteringService().findUsagePoint(id);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        UsagePoint usagePoint = found.get();
        if (!usagePoint.hasAccountability((User) securityContext.getUserPrincipal()) && !((User) securityContext.getUserPrincipal()).hasPrivilege(Privileges.BROWSE_ANY)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return usagePoint;
    }

}
