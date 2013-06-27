package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyTransaction implements Transaction<Party> {

    private final PartyInfo info;

    public UpdatePartyTransaction(PartyInfo info) {
        this.info = info;
    }

    @Override
    public Party perform() {
        Party party = fetchParty();
        validateUpdate(party);
        return doUpdate(party);
    }

    private Party doUpdate(Party party) {
        info.updateParty(party);
        party.save();
        return party;
    }

    private void validateUpdate(Party party) {
        if (party.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Party fetchParty() {
        Optional<Party> party = Bus.getPartyService().findParty(info.id);
        if (party.isPresent()) {
            return party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
