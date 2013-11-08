package com.elster.jupiter.events.rest.impl;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.rest.impl.EventTypeInfos;
import com.google.common.base.Optional;


@Path("/eventtypes")
public class EventTypeResource {
	
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public EventTypeInfos getEventTypes(@Context UriInfo uriInfo) {
	      List<EventType> list = Bus.getEventService().getEventTypes();
	      EventTypeInfos infos = new EventTypeInfos(list);
	      infos.total = list.size();
	      return infos;
	  }
	  
	  @PUT
	  @Path("/{topic}/")
	  @Produces(MediaType.APPLICATION_JSON)
	  @Consumes(MediaType.APPLICATION_JSON)
	  public EventTypeInfos updateEventType(EventTypeInfo info, @PathParam("topic") String topic) {
	      info.topic = topic;
	      Bus.getTransactionService().execute(new UpdateEventTypeTransaction(info));
	      return getEventType(info.topic);
	  }
	  
	  @GET
	  @Path("/{topic}/")
	  @Produces(MediaType.APPLICATION_JSON)
	  public EventTypeInfos getEventType(@PathParam("topic") String topic) {
	      Optional<EventType> eventType = Bus.getEventService().getEventType(topic);
	      if (eventType.isPresent()) {
	          return new EventTypeInfos(eventType.get());
	      }
	      throw new WebApplicationException(Response.Status.NOT_FOUND);
	  }



}
