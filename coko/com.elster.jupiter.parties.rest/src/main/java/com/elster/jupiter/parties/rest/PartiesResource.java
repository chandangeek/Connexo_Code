package com.elster.jupiter.parties.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Operator;

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
import java.util.ArrayList;
import java.util.List;


@Path("/prt")
public class PartiesResource {

    @POST
    @Path("/persons")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfos createPerson(PersonInfo info) {
        PersonInfos result = new PersonInfos();
        result.add(Bus.getTransactionService().execute(new CreatePersonTransaction(info)));
        return result;
    }

    @GET
    @Path("/persons/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfos getPerson(@PathParam("id") long id) {
        Party party = Bus.getPartyService().findParty(id);
        if (party instanceof Person) {
            return new PersonInfos((Person) party);
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/persons")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfos getPersons(@Context UriInfo uriInfo) {
        Query<Party> query = Bus.getPartyService().getPartyQuery();
        RestQuery<Party> restQuery = Bus.getQueryService().wrap(query);
        List<Party> list = restQuery.select(uriInfo.getQueryParameters(), Operator.EQUAL.compare("class", Person.TYPE_IDENTIFIER));
        List<Person> persons = new ArrayList<>(list.size());
        for (Party party : list) {
            persons.add((Person) party);
        }
        PersonInfos infos = new PersonInfos(persons);
        int limit = restQuery.getLimit(uriInfo.getQueryParameters());
        int start = restQuery.getStart(uriInfo.getQueryParameters());
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    @PUT
    @Path("/persons/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PersonInfos updatePerson(PersonInfo info, @PathParam("id") long id) {
    	info.id = id;
        PersonInfos result = new PersonInfos();
        result.add(Bus.getTransactionService().execute(new UpdatePersonTransaction(info)));
        return getPerson(info.id);
    }

    @DELETE
    @Path("/persons/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfos deletePerson(PersonInfo info, @PathParam("id") long id) {
        info.id = id;
        Bus.getTransactionService().execute(new DeletePersonTransaction(info));
        return new PersonInfos();

    }
}
