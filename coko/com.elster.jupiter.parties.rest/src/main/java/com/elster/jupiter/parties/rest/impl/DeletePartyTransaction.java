package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeletePartyTransaction extends VoidTransaction {

    private final PartyInfo info;
    private final PartyService partyService;

    @Inject
    public DeletePartyTransaction(PartyInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    protected void doPerform() {
        Party party = fetchParty();
        validateDelete(party);
        doDelete(party);
    }

    private void doDelete(Party party) {
        party.delete();
    }

    private void validateDelete(Party party) {
        if (party.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Party fetchParty() {
        Optional<Party> party = partyService.findParty(info.id);
        if (!party.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Person) party;
    }

}
