package com.elster.jupiter.osgi.goodies;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/events")
public class EventResource {

	@Inject
	private Events events;
	
	@GET
	@Path("/topics/{topic:.+}")
	@Produces(MediaType.APPLICATION_JSON) 
	public long getCount(@PathParam("topic") String topic) {
		return events.getCount(topic);
	}
	
	@GET
	@Path("/topics")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTopics() {
		return events.getTopics();
	}
	    
}
