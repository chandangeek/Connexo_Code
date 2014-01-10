package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyRepresentationTransaction implements Transaction<PartyRepresentation> {

    private final UserService userService;
    private final PartyService partyService;
    private PartyRepresentationInfo info;
    private final Fetcher fetcher;

    @Inject
    public UpdatePartyRepresentationTransaction(UserService userService, PartyService partyService, PartyRepresentationInfo info) {
        this.userService = userService;
        this.partyService = partyService;
        this.info = info;
        fetcher = new Fetcher(this.partyService, this.userService);
    }

    @Override
    public PartyRepresentation perform() {
        Party party = fetcher.fetchParty(info.partyId);
        PartyRepresentation representation = getRepresentation(party);
        if (representation == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        party.adjustRepresentation(representation, new Interval(info.start, info.end));
        partyService.updateRepresentation(representation);
        return representation;
    }

    private PartyRepresentation getRepresentation(Party party) {
        for (PartyRepresentation representation : party.getCurrentDelegates()) {
            if (representation.getDelegate().getName().equals(info.userInfo.authenticationName)) {
                return representation;
            }
        }
        return null;
    }
}
