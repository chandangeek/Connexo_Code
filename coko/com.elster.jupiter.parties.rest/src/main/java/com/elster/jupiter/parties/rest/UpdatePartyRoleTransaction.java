package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 15:00
 */
public class UpdatePartyRoleTransaction implements Transaction<PartyRole> {
    private final PartyRoleInfo info;

    public UpdatePartyRoleTransaction(PartyRoleInfo info) {
        this.info = info;
    }

    @Override
    public PartyRole perform() {
        String mRID = info.mRID;
        Optional<PartyRole> role = Bus.getPartyService().findPartyRoleByMRID(mRID);
        if (role.isPresent()) {
            return doUpdate(role.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private PartyRole doUpdate(PartyRole partyRole) {
        partyRole.setName(info.name);
        partyRole.setAliasName(info.aliasName);
        partyRole.setDescription(info.description);
        Bus.getPartyService().updateRole(partyRole);
        return partyRole;
    }

    private void validateUpdate(PartyRole partyRole) {
        if (partyRole.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }
}
