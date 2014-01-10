package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;

import javax.inject.Inject;
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
import java.util.List;


@Path("/eventtypes")
public class EventTypeResource {

    private final TransactionService transactionService;
    private final EventService eventService;

    @Inject
    public EventTypeResource(TransactionService transactionService, EventService eventService) {
        this.transactionService = transactionService;
        this.eventService = eventService;
    }

    @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  public EventTypeInfos getEventTypes(@Context UriInfo uriInfo) {
	      List<EventType> list = eventService.getEventTypes();
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
	      transactionService.execute(new UpdateEventTypeTransaction(info, eventService));
	      return getEventType(info.topic);
	  }
	  
	  @GET
	  @Path("/{topic}/")
	  @Produces(MediaType.APPLICATION_JSON)
	  public EventTypeInfos getEventType(@PathParam("topic") String topic) {
	      Optional<EventType> eventType = eventService.getEventType(topic);
	      if (eventType.isPresent()) {
	          return new EventTypeInfos(eventType.get());
	      }
	      throw new WebApplicationException(Response.Status.NOT_FOUND);
	  }



}
