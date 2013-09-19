package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.Transaction;

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
import java.util.List;

@Path("/roles")
public class RolesResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos createPartyRole(final PartyRoleInfo info) {
        PartyRoleInfos result = new PartyRoleInfos();
        result.add(Bus.getTransactionService().execute(new Transaction<PartyRole>() {
            @Override
            public PartyRole perform() {
                return Bus.getPartyService().createRole(info.componentName, info.mRID, info.name, info.aliasName, info.description);
            }
        }));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos deletePartyRole(PartyRoleInfo info, @PathParam("id") String id) {
        info.mRID = id;
        Bus.getTransactionService().execute(new DeletePartyRoleTransaction(info));
        return new PartyRoleInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos getPartyRole(@PathParam("id") String id) {
        List<PartyRole> partyRoles = Bus.getPartyService().getPartyRoles();
        for (PartyRole partyRole : partyRoles) {
            if (partyRole.getMRID().equals(id)) {
                return new PartyRoleInfos(partyRole);
            }
        }
        return new PartyRoleInfos();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PartyRoleInfos getPartyRoles(@Context UriInfo uriInfo) {
        return new PartyRoleInfos(Bus.getPartyService().getPartyRoles());
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PartyRoleInfos updatePartyRole(PartyRoleInfo info, @PathParam("id") String id) {
        info.mRID = id;
        Bus.getTransactionService().execute(new UpdatePartyRoleTransaction(info));
        return getPartyRole(info.mRID);
    }


}
