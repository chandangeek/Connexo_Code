/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;

public class UpdatePartyTransaction implements Transaction<Party> {
    private final PartyInfo info;
    private final Fetcher fetcher;

    @Inject
    public UpdatePartyTransaction(PartyInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public Party perform() {
        return doUpdate(this.fetcher.findAndLockParty(this.info));
    }

    private Party doUpdate(Party party) {
        info.updateParty(party);
        party.update();
        return party;
    }

}
