package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.rest.UserInfoFactory;

import com.google.common.collect.Range;

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
import java.time.Clock;
import java.util.List;
import java.util.Optional;


@Path("/parties")
public class PartiesResource {

    private final PartyService partyService;
    private final TransactionService transactionService;
    private final NlsService nlsService;
    private final RestQueryService restQueryService;
    private final Clock clock;
    private final Fetcher fetcher;
    private final UserInfoFactory userInfoFactory;

    @Inject
    public PartiesResource(PartyService partyService,
                           TransactionService transactionService,
                           NlsService nlsService,
                           RestQueryService restQueryService,
                           Clock clock,
                           Fetcher fetcher, UserInfoFactory userInfoFactory) {
        this.partyService = partyService;
        this.transactionService = transactionService;
        this.nlsService = nlsService;
        this.restQueryService = restQueryService;
        this.clock = clock;
        this.fetcher = fetcher;
        this.userInfoFactory = userInfoFactory;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyInfos deleteParty(PartyInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeletePartyTransaction(info, fetcher));
        return new PartyInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyInfos getParty(@PathParam("id") long id) {
        return new PartyInfos(partyWithId(id));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyInfos getParties(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Party> parties = getPartyRestQuery().select(queryParameters);
        PartyInfos infos = new PartyInfos(queryParameters.clipToLimit(parties));
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyInfos updateParty(PartyInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdatePartyTransaction(info, fetcher));
        return getParty(info.id);
    }

    @GET
    @Path("/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyInRoleInfos getRoles(@PathParam("id") long id) {
        Party party = partyWithId(id);
        return new PartyInRoleInfos(party.getPartyInRoles(Range.all()));
    }

    @POST
    @Path("/{id}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyInRoleInfos addRole(@PathParam("id") long id, PartyInRoleInfo info) {
        PartyInRoleInfos result = new PartyInRoleInfos();
        result.add(transactionService.execute(new AddRoleTransaction(id, info, fetcher)));
        return result;
    }

    @PUT
    @Path("/{id}/roles/{roleId}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos updatePartyInRole(@PathParam("id") long id, @PathParam("roleId") long roleId, PartyInRoleInfo info) {
        info.partyId = id;
        info.id = roleId;
        PartyInRole partyInRole = transactionService.execute(new TerminatePartyRoleTransaction(info, fetcher));
        return new PartyInRoleInfos(partyInRole);
    }

    @GET
    @Path("/{id}/delegates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyRepresentationInfos getDelegates(@PathParam("id") long id) {
        try (TransactionContext context = transactionService.getContext()) {
            try {
                Party party = partyWithId(id);
                return new PartyRepresentationInfos(party.getCurrentDelegates(), this.nlsService, userInfoFactory);
            } finally {
                context.commit();
            }
        }
    }

    @POST
    @Path("/{id}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyRepresentationInfos addRole(@PathParam("id") long id, PartyRepresentationInfo info) {
        transactionService.execute(new AddDelegateTransaction(id, info, fetcher));
        return getDelegates(id);
    }

    @PUT
    @Path("/{id}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PartyRepresentationInfos updateRoles(@PathParam("id") long id, PartyRepresentationInfos infos) {
        new UpdatePartyRepresentationsTransaction(id, infos, clock, this.nlsService, fetcher, userInfoFactory); // doesn't work
        return getDelegates(id);
    }

    @PUT
    @Path("/{id}/delegates/{authenticationName}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyRepresentationInfos updatePartyInRole(PartyRepresentationInfo info, @PathParam("id") long id, @PathParam("authenticationName") String authenticationName) {
        info.partyId = id;
        info.userInfo.authenticationName = authenticationName;
        try (TransactionContext context = transactionService.getContext()) {
            PartyRepresentation partyRepresentation = transactionService.execute(new UpdatePartyRepresentationTransaction(info, fetcher));
            context.commit();
            return new PartyRepresentationInfos(partyRepresentation, this.nlsService, userInfoFactory);
        }
    }


    private RestQuery<Party> getPartyRestQuery() {
        Query<Party> query = partyService.getPartyQuery();
        return restQueryService.wrap(query);
    }

}