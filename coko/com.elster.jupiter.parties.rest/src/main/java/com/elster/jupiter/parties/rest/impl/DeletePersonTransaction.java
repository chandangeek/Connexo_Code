/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Person;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.inject.Inject;

class DeletePersonTransaction extends VoidTransaction {

    private final PersonInfo info;
    private final Fetcher fetcher;

    @Inject
    public DeletePersonTransaction(PersonInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    protected void doPerform() {
        doDelete(fetchPerson());
    }

    private void doDelete(Person person) {
        person.delete();
    }

    private Person fetchPerson() {
        return (Person) fetcher.findAndLockParty(info);
    }
}
