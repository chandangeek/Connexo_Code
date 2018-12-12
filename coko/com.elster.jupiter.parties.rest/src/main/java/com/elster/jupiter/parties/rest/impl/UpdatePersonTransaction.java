/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class UpdatePersonTransaction implements Transaction<Person> {

    private final PersonInfo info;
    private final Fetcher fetcher;

    @Inject
    public UpdatePersonTransaction(PersonInfo info, Fetcher fetcher) {
        assert info != null;
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public Person perform() {
        return doUpdate(fetchPerson());
    }

    private Person doUpdate(Person person) {
        info.update(person);
        person.update();
        return person;
    }

    private Person fetchPerson() {
        Party party = fetcher.findAndLockParty(this.info);
        if (party instanceof Person) {
            return (Person) party;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
