package com.elster.jupiter.parties.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.google.common.base.Optional;

import javax.ws.rs.Consumes;
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


@Path("/prt/parties")
public class PartiesResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos getParties(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Party> parties = getPartyRestQuery().select(queryParameters);
        PartyInfos infos = new PartyInfos(parties);
        infos.total = determineTotal(queryParameters, parties);
        return infos;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos getParties(@PathParam("id") long id) {
        return new PartyInfos(partyWithId(id));
    }

    private Party partyWithId(long id) {
        Optional<Party> party = Bus.getPartyService().findParty(id);
        if (party.isPresent()) {
            return party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos getRoles(@PathParam("id") long id) {
        Party party = partyWithId(id);
        return new PartyInRoleInfos(party.getPartyInRoles());
    }

    @POST
    @Path("/{id}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos addRole(@PathParam("id") long id, PartyInRoleInfo info) {
        PartyInRoleInfos result = new PartyInRoleInfos();
        result.add(Bus.getTransactionService().execute(new AddRoleTransaction(id, info)));
        return result;
    }

    @PUT
    @Path("/{id}/roles/{roleId}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos updatePartyInRole(PartyInRoleInfo info, @PathParam("id") long id,  @PathParam("roleId") long roleId) {
        info.partyId = id;
        info.id = roleId;
        PartyInRole partyInRole = Bus.getTransactionService().execute(new TerminatePartyRoleTransaction(info));
        return new PartyInRoleInfos(partyInRole);
    }

    private int determineTotal(QueryParameters queryParameters, List<Party> list) {
        int total = queryParameters.getStart() + list.size();
        if (list.size() == queryParameters.getLimit()) {
            total++;
        }
        return total;
    }

    private RestQuery<Party> getPartyRestQuery() {
        Query<Party> query = Bus.getPartyService().getPartyQuery();
        return Bus.getQueryService().wrap(query);
    }
}
