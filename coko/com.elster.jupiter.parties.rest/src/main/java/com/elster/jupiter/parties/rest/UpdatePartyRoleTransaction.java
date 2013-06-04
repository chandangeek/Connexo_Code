package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyRoleTransaction implements Transaction<PartyRole> {

    private final PartyRoleInfo info;

    public UpdatePartyRoleTransaction(PartyRoleInfo info) {
        this.info = info;
    }

    @Override
    public PartyRole perform() {
        PartyRole partyRole = fetchRole();
        validateUpdate(partyRole);
        return doUpdate(partyRole);
    }

    private PartyRole fetchRole() {
        Optional<PartyRole> role = Bus.getPartyService().findPartyRoleByMRID(info.mRID);
        if (role.isPresent()) {
            return role.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private void validateUpdate(PartyRole role) {
        if (role.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private PartyRole doUpdate(PartyRole partyRole) {
    	partyRole.setName(info.name);
        partyRole.setAliasName(info.aliasName);
        partyRole.setDescription(info.description);
        Bus.getPartyService().updateRole(partyRole);
        return partyRole;
    }

}
