package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
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

import java.util.List;


@Path("/parties")
public class PartiesResource {

    private final PartyService partyService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final Clock clock;

    @Inject
    public PartiesResource(PartyService partyService, TransactionService transactionService, UserService userService, RestQueryService restQueryService, Clock clock) {
        this.partyService = partyService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.clock = clock;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos deleteParty(PartyInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeletePartyTransaction(info, partyService));
        return new PartyInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos getParty(@PathParam("id") long id) {
        return new PartyInfos(partyWithId(id));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos getParties(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Party> parties = getPartyRestQuery().select(queryParameters);
        PartyInfos infos = new PartyInfos(parties);
        infos.total = queryParameters.determineTotal(parties.size());
        return infos;
    }

    private Party partyWithId(long id) {
        Optional<Party> party = partyService.findParty(id);
        if (party.isPresent()) {
            return party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyInfos updateParty(PartyInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdatePartyTransaction(info, partyService));
        return getParty(info.id);
    }

    @GET
    @Path("/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos getRoles(@PathParam("id") long id) {
        Party party = partyWithId(id);
        return new PartyInRoleInfos(party.getPartyInRoles(Interval.sinceEpoch()));
    }

    @POST
    @Path("/{id}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos addRole(@PathParam("id") long id, PartyInRoleInfo info) {
        PartyInRoleInfos result = new PartyInRoleInfos();
        result.add(transactionService.execute(new AddRoleTransaction(id, info, userService, partyService)));
        return result;
    }

    @PUT
    @Path("/{id}/roles/{roleId}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos updatePartyInRole(PartyInRoleInfo info, @PathParam("id") long id,  @PathParam("roleId") long roleId) {
        info.partyId = id;
        info.id = roleId;
        PartyInRole partyInRole = transactionService.execute(new TerminatePartyRoleTransaction(info, userService, partyService));
        return new PartyInRoleInfos(partyInRole);
    }

    @GET
    @Path("/{id}/delegates")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRepresentationInfos getDelegates(@PathParam("id") long id) {
        Party party = partyWithId(id);
        return new PartyRepresentationInfos(party.getCurrentDelegates());
    }

    @POST
    @Path("/{id}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRepresentationInfos addRole(@PathParam("id") long id, PartyRepresentationInfo info) {
        transactionService.execute(new AddDelegateTransaction(id, info, userService, partyService));
        return getDelegates(id);
    }

    @PUT
    @Path("/{id}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRepresentationInfos updateRoles(@PathParam("id") long id, PartyRepresentationInfos infos) {
        new UpdatePartyRepresentationsTransaction(id, infos, partyService, clock, userService);
        return getDelegates(id);
    }

    @PUT
    @Path("/{id}/delegates/{authenticationName}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyRepresentationInfos updatePartyInRole(PartyRepresentationInfo info, @PathParam("id") long id,  @PathParam("authenticationName") String authenticationName) {
        info.partyId = id;
        info.userInfo.authenticationName = authenticationName;
        PartyRepresentation partyRepresentation = transactionService.execute(new UpdatePartyRepresentationTransaction(userService, partyService, info));
        return new PartyRepresentationInfos(partyRepresentation);
    }


    private RestQuery<Party> getPartyRestQuery() {
        Query<Party> query = partyService.getPartyQuery();
        return restQueryService.wrap(query);
    }
}
