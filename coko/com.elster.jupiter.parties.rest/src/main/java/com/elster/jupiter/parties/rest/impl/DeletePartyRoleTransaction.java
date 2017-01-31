/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.inject.Inject;

public class DeletePartyRoleTransaction extends VoidTransaction {

    private final PartyRoleInfo info;
    private final PartyService partyService;
    private final Fetcher fetcher;

    @Inject
    public DeletePartyRoleTransaction(PartyRoleInfo info, PartyService partyService, Fetcher fetcher) {
        this.info = info;
        this.partyService = partyService;
        this.fetcher = fetcher;
    }

    @Override
    public void doPerform() {
        partyService.deletePartyRole(fetcher.findAndLockPartyRole(info));
    }
}
