package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeletePartyTransaction extends VoidTransaction {

    private final PartyInfo info;

    public DeletePartyTransaction(PartyInfo info) {
        this.info = info;
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
        Optional<Party> party = Bus.getPartyService().findParty(info.id);
        if (!party.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Person) party;
    }

}
