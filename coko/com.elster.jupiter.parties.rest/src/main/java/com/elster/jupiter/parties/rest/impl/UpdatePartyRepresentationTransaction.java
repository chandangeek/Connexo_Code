package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.time.Interval;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyRepresentationTransaction implements Transaction<PartyRepresentation> {

    private PartyRepresentationInfo info;
    private Fetcher fetcher = new Fetcher();

    public UpdatePartyRepresentationTransaction(PartyRepresentationInfo info) {
        this.info = info;
    }

    @Override
    public PartyRepresentation perform() {
        Party party = fetcher.fetchParty(info.partyId);
        PartyRepresentation representation = getRepresentation(party);
        if (representation == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        representation.setInterval(new Interval(info.start, info.end));
        Bus.getPartyService().updateRepresentation(representation);
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
