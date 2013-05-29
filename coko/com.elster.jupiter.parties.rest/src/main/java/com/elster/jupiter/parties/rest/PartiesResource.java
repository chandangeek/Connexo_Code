package com.elster.jupiter.parties.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Operator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;


@Path("/prt")
public class PartiesResource {

    @GET
    @Path("/persons")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonInfos getPersons(@Context UriInfo uriInfo) {
        Query<Party> query = Bus.getPartyService().getPartyQuery();
        RestQuery<Party> restQuery = Bus.getQueryService().wrap(query);
        List<Party> list =  restQuery.select(uriInfo.getQueryParameters(), Operator.EQUAL.compare("class", Person.TYPE_IDENTIFIER));
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

    @POST
    @Path("/persons")
    @Consumes(MediaType.APPLICATION_JSON)
    public PersonInfos createUsagePoint(PersonInfo info) {
        PersonInfos result = new PersonInfos();
        result.add(new CreatePersonTransaction(info).execute());
        return result;
    }

}
