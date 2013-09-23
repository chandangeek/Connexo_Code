package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class DeletePartyRoleTransaction extends VoidTransaction {

    private final PartyRoleInfo info;

    public DeletePartyRoleTransaction(PartyRoleInfo info) {
        this.info = info;
    }

    @Override
    public void doPerform() {
        List<PartyRole> partyRoles = Bus.getPartyService().getPartyRoles();
        for (PartyRole partyRole : partyRoles) {
            if (partyRole.getMRID().equals(info.mRID)) {
                Bus.getPartyService().deletePartyRole(partyRole);
                return;
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
