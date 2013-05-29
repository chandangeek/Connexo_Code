package com.elster.jupiter.metering.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.RestQuery;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.elster.jupiter.metering.rest.Bus.getMeteringService;
import static com.elster.jupiter.metering.rest.Bus.getQueryService;

@Path("/mtr")
public class MeteringResource {	

    @GET
	@Path("/usagepoints")
	@Produces(MediaType.APPLICATION_JSON) 
	public UsagePointInfos getUsagePoints(@Context UriInfo uriInfo) {
		Query<UsagePoint> query = getMeteringService().getUsagePointQuery();
		query.setLazy("serviceLocation");
		RestQuery<UsagePoint> restQuery = getQueryService().wrap(query);
		List<UsagePoint> list =  restQuery.select(uriInfo.getQueryParameters());
		UsagePointInfos infos = new UsagePointInfos(list);
		infos.addServiceLocationInfo();
		int limit = restQuery.getLimit(uriInfo.getQueryParameters());
		int start = restQuery.getStart(uriInfo.getQueryParameters());
		infos.total = start + list.size();
		if (list.size() == limit) {
			infos.total++;
		}
		return infos;
	  }
	  
	@PUT
	@RolesAllowed("user")
	@Path("/usagepoints/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos updateUsagePoint(UsagePointInfo info) {
        Bus.getServiceLocator().getTransactionService().execute(new UpdateUsagePointTransaction(info));
        return getUsagePoint(info.id);
	}
	  
	@GET
	@Path("/usagepoints/{id}/")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos getUsagePoint(@PathParam("id") long id) {
		UsagePoint usagePoint = Bus.getMeteringService().findUsagePoint(id);
		if (usagePoint == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		UsagePointInfos result = new UsagePointInfos(usagePoint);
		result.addServiceLocationInfo();
		return result;
	}
	  
	  
	@POST
	@Path("/usagepoints")
	@Consumes(MediaType.APPLICATION_JSON) 
	public UsagePointInfos createUsagePoint(UsagePointInfo info) {
		UsagePointInfos result = new UsagePointInfos();
        result.add(Bus.getTransactionService().execute(new CreateUsagePointTransaction(info)));
		return result;
	}	    
}
