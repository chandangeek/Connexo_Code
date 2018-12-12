/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.inject.Inject;

public class DeletePartyTransaction extends VoidTransaction {
    private final PartyInfo info;
    private final Fetcher fetcher;

    @Inject
    public DeletePartyTransaction(PartyInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    protected void doPerform() {
        doDelete(this.fetcher.findAndLockParty(this.info));
    }

    private void doDelete(Party party) {
        party.delete();
    }
}
