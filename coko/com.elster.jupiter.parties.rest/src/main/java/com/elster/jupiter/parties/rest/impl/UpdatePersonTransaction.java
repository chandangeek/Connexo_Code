package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class UpdatePersonTransaction implements Transaction<Person> {

    private final PersonInfo info;
    private final PartyService partyService;

    @Inject
    public UpdatePersonTransaction(PersonInfo info, PartyService partyService) {
        this.partyService = partyService;
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
        Optional<Party> party = partyService.findParty(info.id);
        if (party.isPresent() && party.get() instanceof Person) {
            return (Person) party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
