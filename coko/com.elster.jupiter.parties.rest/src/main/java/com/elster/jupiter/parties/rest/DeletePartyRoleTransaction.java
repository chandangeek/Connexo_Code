package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 14:20
 */
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
