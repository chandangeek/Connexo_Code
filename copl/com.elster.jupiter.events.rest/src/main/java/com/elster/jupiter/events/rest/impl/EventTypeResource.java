/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/eventtypes")
public class EventTypeResource {

    private final TransactionService transactionService;
    private final EventService eventService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public EventTypeResource(TransactionService transactionService, EventService eventService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.transactionService = transactionService;
        this.eventService = eventService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getEventTypes(@BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromCompleteList("eventTypes", wrapDomainObjects(eventService.getEventTypes()), queryParameters);
    }

    @PUT
    @Path("/{topic}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList updateEventType(EventTypeInfo info, @PathParam("topic") String topic, @BeanParam JsonQueryParameters queryParameters) {
        info.topic = topic;
        transactionService.execute(new UpdateEventTypeTransaction(info, eventService, conflictFactory));
        return getEventType(info.topic, queryParameters);
    }

    @GET
    @Path("/{topic}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getEventType(@PathParam("topic") String topic, @BeanParam JsonQueryParameters queryParameters) {
        Optional<EventType> eventType = eventService.getEventType(topic);
        if (eventType.isPresent()) {
            return PagedInfoList.fromCompleteList("eventTypes", wrapDomainObjects(Collections.singletonList(eventType.get())), queryParameters);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private List<EventTypeInfo> wrapDomainObjects(List<EventType> eventTypes) {
        return eventTypes != null ? eventTypes.
                stream()
                .filter(Objects::nonNull)
                .map(EventTypeInfo::new)
                .collect(Collectors.toList()) : Collections.emptyList();
    }
}