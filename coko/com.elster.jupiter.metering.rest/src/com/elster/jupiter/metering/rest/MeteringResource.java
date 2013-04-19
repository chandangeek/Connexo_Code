package com.elster.jupiter.metering.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.elster.jupiter.metering.*;

import static com.elster.jupiter.metering.rest.Bus.*;

@Path("/mtr")
public class MeteringResource {	
	@GET
	@Produces(MediaType.TEXT_HTML)
	  public String component() {
	    return "<html> " + "<title>" + "Metering" + "</title>"
	    + "<body><h1>" + "Metering component2" + "</body></h1>" + "</html> ";
	}
	  
	@GET
	@Path("/usagepoints")
	@Produces(MediaType.APPLICATION_JSON) 
	public UsagePointInfos getUsagePoints(@Context UriInfo uriInfo) {
		UsagePointInfos infos = new UsagePointInfos(getQueryService().wrap(getMeteringService().getUsagePointQuery()).where(uriInfo.getQueryParameters()));
		infos.addServiceLocationInfo();
		return infos;
	  }
	  
	@PUT
	@RolesAllowed("user")
	@Path("/usagepoints/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos updateUsagePoint(UsagePointInfo info) {
		new UpdateUsagePointTransaction(info).execute();
		return getUsagePoint(info.id);
	}
	  
	@GET
	@Path("/usagepoints/{id}/")
	@Produces(MediaType.APPLICATION_JSON)
	public UsagePointInfos getUsagePoint(@PathParam("id") long id) {
		UsagePoint usagePoint = Bus.getServiceLocator().getMeteringService().findUsagePoint(id);
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
		result.add(new CreateUsagePointTransaction(info).execute());
		return result;
	}	    
}
