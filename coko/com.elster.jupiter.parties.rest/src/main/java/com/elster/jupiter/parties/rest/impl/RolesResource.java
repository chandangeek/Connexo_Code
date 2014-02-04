package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/roles")
public class RolesResource {

    private final TransactionService transactionService;
    private final PartyService partyService;

    @Inject
    public RolesResource(TransactionService transactionService, PartyService partyService) {
        this.transactionService = transactionService;
        this.partyService = partyService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos createPartyRole(final PartyRoleInfo info) {
        PartyRoleInfos result = new PartyRoleInfos();
        result.add(transactionService.execute(new Transaction<PartyRole>() {
            @Override
            public PartyRole perform() {
                return partyService.createRole(info.componentName, info.mRID, info.name, info.aliasName, info.description);
            }
        }));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos deletePartyRole(PartyRoleInfo info, @PathParam("id") String id) {
        info.mRID = id;
        transactionService.execute(new DeletePartyRoleTransaction(info, partyService));
        return new PartyRoleInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos getPartyRole(@PathParam("id") String id) {
        Optional<PartyRole> found = partyService.findPartyRoleByMRID(id);
        if (!found.isPresent()) {
            return new PartyRoleInfos();
        }
        return new PartyRoleInfos(found.get());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos getPartyRoles(@Context UriInfo uriInfo) {
        return new PartyRoleInfos(partyService.getPartyRoles());
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyRoleInfos updatePartyRole(PartyRoleInfo info, @PathParam("id") String id) {
        info.mRID = id;
        transactionService.execute(new UpdatePartyRoleTransaction(info, partyService));
        return getPartyRole(info.mRID);
    }


}
