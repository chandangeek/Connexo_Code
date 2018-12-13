/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UpdatePartyRepresentationTransaction implements Transaction<PartyRepresentation> {
    private final PartyRepresentationInfo info;
    private final Fetcher fetcher;

    @Inject
    public UpdatePartyRepresentationTransaction(PartyRepresentationInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public PartyRepresentation perform() {
        Party party = fetcher.findAndLockParty(info.parent);
        PartyRepresentation representation = fetcher.findAndLockPartyRepresentation(info);
        party.adjustRepresentation(representation, Range.closedOpen(info.start, info.end));
        return representation;
    }


}
