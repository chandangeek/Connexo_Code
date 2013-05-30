package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class DeletePersonTransaction extends VoidTransaction {

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

    private Person doDelete(Person person) {
        person.delete();
        return person;
    }

    private void validateDelete(Person person) {
        if (person.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Person fetchPerson() {
        Party party = Bus.getPartyService().findParty(info.id);
        if (!(party instanceof Person)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Person) party;
    }
}
