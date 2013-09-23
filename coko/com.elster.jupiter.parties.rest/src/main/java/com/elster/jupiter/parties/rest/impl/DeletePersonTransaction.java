package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class DeletePersonTransaction extends VoidTransaction {

    private final PersonInfo info;

    public DeletePersonTransaction(PersonInfo info) {
        this.info = info;
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
        Optional<Party> party = Bus.getPartyService().findParty(info.id);
        if (!party.isPresent() || !(party.get() instanceof Person)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Person) party;
    }

}
