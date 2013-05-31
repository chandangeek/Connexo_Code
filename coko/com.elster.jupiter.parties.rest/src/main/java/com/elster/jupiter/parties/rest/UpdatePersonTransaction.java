package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class UpdatePersonTransaction implements Transaction<Person> {

    private final PersonInfo info;

    public UpdatePersonTransaction(PersonInfo info) {
        assert info != null;
        this.info = info;
    }

    @Override
    public Person perform() {
        Person person = fetchPerson();
        validateUpdate(person);
        return doUpdate(person);
    }

    private Person doUpdate(Person person) {
        info.update(person);
        person.save();
        return person;
    }

    private void validateUpdate(Person person) {
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
