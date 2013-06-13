package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TerminatePartyRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final Fetcher fetcher = new Fetcher();

    public TerminatePartyRoleTransaction(PartyInRoleInfo info) {
        this.info = info;
    }

    @Override
    public PartyInRole perform() {
        Party party = fetcher.fetchParty(info.partyId);
        PartyInRole partyInRole = getPartyInRole(party);
        party.terminateRole(partyInRole, info.end);
        return partyInRole;
    }

    private PartyInRole getPartyInRole(Party party) {
        for (PartyInRole partyInRole : party.getPartyInRoles()) {
            if (partyInRole.getId() == info.id) {
                if (partyInRole.getVersion() != info.version) {
                    throw new WebApplicationException(Response.Status.CONFLICT);
                }
                return partyInRole;
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
