package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeletePartyRoleTransaction extends VoidTransaction {

    private final PartyRoleInfo info;

    public DeletePartyRoleTransaction(PartyRoleInfo info) {
        this.info = info;
    }

    @Override
    public void doPerform() {
        Optional<PartyRole> found = Bus.getPartyService().findPartyRoleByMRID(info.mRID);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Bus.getPartyService().deletePartyRole(found.get());
    }
}
