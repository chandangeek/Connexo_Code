package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.base.Optional;

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
import java.util.ArrayList;
import java.util.List;

@Path("/organizations")
public class OrganizationsResource {

    private final TransactionService transactionService;
    private final PartyService partyService;
    private RestQueryService restQueryService;

    @Inject
    public OrganizationsResource(TransactionService transactionService, PartyService partyService) {
        this.transactionService = transactionService;
        this.partyService = partyService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationInfos createOrganization(OrganizationInfo info) {
        OrganizationInfos result = new OrganizationInfos();
        result.add(transactionService.execute(new CreateOrganizationTransaction(info, partyService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationInfos deleteOrganization(OrganizationInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeleteOrganizationTransaction(info, partyService));
        return new OrganizationInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationInfos getOrganization(@PathParam("id") long id) {
        Optional<Party> party = partyService.findParty(id);
        if (party.isPresent() && party.get() instanceof Organization) {
            return new OrganizationInfos((Organization) party.get());
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationInfos getOrganizations(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Party> list = getPartyRestQuery().select(queryParameters, Operator.EQUAL.compare("class", Organization.TYPE_IDENTIFIER));
        List<Organization> organizations = new ArrayList<>(list.size());
        for (Party party : list) {
            organizations.add((Organization) party);
        }
        OrganizationInfos infos = new OrganizationInfos(queryParameters.clipToLimit(organizations));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public OrganizationInfos updateOrganization(OrganizationInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateOrganizationTransaction(info, partyService));
        return getOrganization(info.id);
    }

    private RestQuery<Party> getPartyRestQuery() {
        Query<Party> query = partyService.getPartyQuery();
        return restQueryService.wrap(query);
    }

}
