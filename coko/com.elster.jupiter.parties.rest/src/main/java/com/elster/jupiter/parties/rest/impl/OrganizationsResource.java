/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
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

@Path("/organizations")
public class OrganizationsResource {

    private final TransactionService transactionService;
    private final PartyService partyService;
    private final RestQueryService restQueryService;
    private final Fetcher fetcher;

    @Inject
    public OrganizationsResource(TransactionService transactionService, PartyService partyService, Fetcher fetcher, RestQueryService restQueryService) {
        this.transactionService = transactionService;
        this.partyService = partyService;
        this.restQueryService = restQueryService;
        this.fetcher = fetcher;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public OrganizationInfos createOrganization(OrganizationInfo info) {
        OrganizationInfos result = new OrganizationInfos();
        result.add(transactionService.execute(new CreateOrganizationTransaction(info, partyService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public OrganizationInfos deleteOrganization(@PathParam("id") long id, OrganizationInfo info) {
        info.id = id;
        transactionService.execute(new DeleteOrganizationTransaction(info, fetcher));
        return new OrganizationInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public OrganizationInfos getOrganization(@PathParam("id") long id) {
        Optional<Party> party = partyService.findParty(id);
        if (party.isPresent() && party.get() instanceof Organization) {
            return new OrganizationInfos((Organization) party.get());
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public OrganizationInfos getOrganizations(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Organization> organizations = getOrganizationRestQuery().select(queryParameters);
        OrganizationInfos infos = new OrganizationInfos(queryParameters.clipToLimit(organizations));
        infos.total = queryParameters.determineTotal(organizations.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public OrganizationInfos updateOrganization(OrganizationInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateOrganizationTransaction(info, fetcher));
        return getOrganization(info.id);
    }

    private RestQuery<Organization> getOrganizationRestQuery() {
        return restQueryService.wrap(partyService.getOrganizationQuery());
    }

}