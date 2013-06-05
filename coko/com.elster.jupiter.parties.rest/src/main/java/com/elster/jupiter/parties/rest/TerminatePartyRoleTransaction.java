package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 5/06/13
 * Time: 10:10
 */
public class TerminatePartyRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;

    public TerminatePartyRoleTransaction(PartyInRoleInfo info) {
        this.info = info;
    }

    @Override
    public PartyInRole perform() {
        Optional<Party> party = Bus.getPartyService().findParty(info.partyId);
        if (party.isPresent()) {
            PartyInRole partyInRole = getPartyInRole(party.get());
            party.get().terminateRole(partyInRole, info.end);
            return partyInRole;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    private PartyInRole getPartyInRole(Party party) {
        for (PartyInRole partyInRole : party.getPartyInRoles()) {
            if (partyInRole.getId() == info.id) {
                return partyInRole;
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
