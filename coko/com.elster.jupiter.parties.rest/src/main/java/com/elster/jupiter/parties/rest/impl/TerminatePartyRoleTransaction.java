/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class TerminatePartyRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final Fetcher fetcher;

    @Inject
    public TerminatePartyRoleTransaction(PartyInRoleInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public PartyInRole perform() {
        Party party = fetcher.findAndLockParty(info.parent);
        PartyInRole partyInRole = fetcher.findAndLockPartyInRole(info);
        party.terminateRole(partyInRole, info.end);
        return partyInRole;
    }
}
