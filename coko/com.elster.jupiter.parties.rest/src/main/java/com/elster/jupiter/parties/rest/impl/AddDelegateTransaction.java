/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class AddDelegateTransaction implements Transaction<PartyRepresentation> {

    private final long partyId;
    private final PartyRepresentationInfo info;
    private final Fetcher fetcher;

    public AddDelegateTransaction(long partyId, PartyRepresentationInfo info, Fetcher fetcher) {
        this.partyId = partyId;
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public PartyRepresentation perform() {
        Party party = fetcher.fetchParty(partyId);
        User user = fetcher.fetchUser(info.userInfo.authenticationName);

        return party.appointDelegate(user, info.start);
    }
}
