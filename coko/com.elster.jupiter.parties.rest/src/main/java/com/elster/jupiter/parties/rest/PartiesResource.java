package com.elster.jupiter.parties.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
        infos.total = queryParameters.determineTotal(parties.size());
        return infos;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInfos getParties(@PathParam("id") long id) {
        return new PartyInfos(partyWithId(id));
    }

    private Party partyWithId(long id) {
        return Bus.getPartyService().findParty(id);
    }

    @GET
    @Path("/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyInRoleInfos getRoles(@PathParam("id") long id, @Context UriInfo uriInfo) {
        return new PartyInRoleInfos(partyWithId(id).getPartyInRoles());
    }

    private RestQuery<Party> getPartyRestQuery() {
        Query<Party> query = Bus.getPartyService().getPartyQuery();
        return Bus.getQueryService().wrap(query);
    }
}
