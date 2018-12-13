/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;


public class AddRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final long partyId;
    private final Fetcher fetcher;

    @Inject
    public AddRoleTransaction(long partyId, PartyInRoleInfo info, Fetcher fetcher) {
        this.partyId = partyId;
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public PartyInRole perform() {
        Party party = fetcher.fetchParty(partyId);
        PartyRole role = fetcher.fetchRole(info.roleMRID);

        return party.assumeRole(role, info.start);
    }

}