package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class DeletePersonTransaction extends VoidTransaction {

    private final PersonInfo info;
    private final PartyService partyService;

    @Inject
    public DeletePersonTransaction(PersonInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    protected void doPerform() {
        Person person = fetchPerson();
        validateDelete(person);
        doDelete(person);
    }

    private void doDelete(Person person) {
        person.delete();
    }

    private void validateDelete(Person person) {
        if (person.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Person fetchPerson() {
        Optional<Party> party = partyService.findParty(info.id);
        if (!party.isPresent() || !(party.get() instanceof Person)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Person) party;
    }

}
