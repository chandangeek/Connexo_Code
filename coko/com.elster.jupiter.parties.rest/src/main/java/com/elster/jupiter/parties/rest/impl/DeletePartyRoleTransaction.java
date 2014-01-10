package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeletePartyRoleTransaction extends VoidTransaction {

    private final PartyRoleInfo info;
    private final PartyService partyService;

    @Inject
    public DeletePartyRoleTransaction(PartyRoleInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    public void doPerform() {
        Optional<PartyRole> found = partyService.findPartyRoleByMRID(info.mRID);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        partyService.deletePartyRole(found.get());
    }
}
