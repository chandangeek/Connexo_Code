package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TerminatePartyRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final Fetcher fetcher;
    private final UserService userService;
    private final PartyService partyService;

    @Inject
    public TerminatePartyRoleTransaction(PartyInRoleInfo info, UserService userService, PartyService partyService) {
        this.info = info;
        this.userService = userService;
        this.partyService = partyService;
        fetcher = new Fetcher(this.partyService, this.userService);
    }

    @Override
    public PartyInRole perform() {
        Party party = fetcher.fetchParty(info.partyId);
        PartyInRole partyInRole = getPartyInRole(party);
        party.terminateRole(partyInRole, info.end);
        return partyInRole;
    }

    private PartyInRole getPartyInRole(Party party) {
        for (PartyInRole partyInRole : party.getPartyInRoles(Interval.sinceEpoch())) {
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
