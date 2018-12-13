/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import java.util.Optional;

@Path("/persons")
public class PersonsResource {

    private final TransactionService transactionService;
    private final PartyService partyService;
    private final RestQueryService restQueryService;
    private final Fetcher fetcher;

    @Inject
    public PersonsResource(TransactionService transactionService, PartyService partyService, RestQueryService restQueryService, Fetcher fetcher) {
        this.transactionService = transactionService;
        this.partyService = partyService;
        this.restQueryService = restQueryService;
        this.fetcher = fetcher;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PersonInfos createPerson(PersonInfo info) {
        PersonInfos result = new PersonInfos();
        result.add(transactionService.execute(new CreatePersonTransaction(info, partyService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PersonInfos deletePerson(@PathParam("id") long id, PersonInfo info) {
        info.id = id;
        transactionService.execute(new DeletePersonTransaction(info, fetcher));
        return new PersonInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PersonInfos getPerson(@PathParam("id") long id) {
        Optional<Party> party = partyService.findParty(id);
        if (party.isPresent() && party.get() instanceof Person) {
            return new PersonInfos((Person) party.get());
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PersonInfos getPersons(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Person> persons = getPersonRestQuery().select(queryParameters);
        PersonInfos infos = new PersonInfos(queryParameters.clipToLimit(persons));
        infos.total = queryParameters.determineTotal(persons.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PersonInfos updatePerson(PersonInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdatePersonTransaction(info, fetcher));
        return getPerson(info.id);
    }

    private RestQuery<Person> getPersonRestQuery() {
        return restQueryService.wrap(partyService.getPersonQuery());
    }

}
