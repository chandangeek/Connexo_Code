/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;

public class UpdatePartyRoleTransaction implements Transaction<PartyRole> {

    private final PartyRoleInfo info;
    private final PartyService partyService;
    private final Fetcher fetcher;

    @Inject
    public UpdatePartyRoleTransaction(PartyRoleInfo info, PartyService partyService, Fetcher fetcher) {
        this.info = info;
        this.partyService = partyService;
        this.fetcher = fetcher;
    }

    @Override
    public PartyRole perform() {
        return doUpdate(fetchRole());
    }

    private PartyRole fetchRole() {
        return fetcher.findAndLockPartyRole(this.info);
    }

    private PartyRole doUpdate(PartyRole partyRole) {
    	partyRole.setName(info.name);
        partyRole.setAliasName(info.aliasName);
        partyRole.setDescription(info.description);
        partyService.updateRole(partyRole);
        return partyRole;
    }

}
