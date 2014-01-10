package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyTransaction implements Transaction<Party> {

    private final PartyInfo info;
    private final PartyService partyService;

    @Inject
    public UpdatePartyTransaction(PartyInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    public Party perform() {
        Party party = fetchParty();
        validateUpdate(party);
        return doUpdate(party);
    }

    private Party fetchParty() {
        Optional<Party> party = partyService.findParty(info.id);
        if (party.isPresent()) {
            return party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private void validateUpdate(Party party) {
        if (party.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Party doUpdate(Party party) {
        info.updateParty(party);
        party.save();
        return party;
    }

}
