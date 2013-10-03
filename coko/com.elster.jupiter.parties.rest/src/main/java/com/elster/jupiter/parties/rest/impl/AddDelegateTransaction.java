package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

public class AddDelegateTransaction implements Transaction<PartyRepresentation> {

    private final long partyId;
    private final PartyRepresentationInfo info;
    private final Fetcher fetcher = new Fetcher();


    public AddDelegateTransaction(long partyId, PartyRepresentationInfo info) {
        this.partyId = partyId;
        this.info = info;
    }

    @Override
    public PartyRepresentation perform() {
        Party party = fetcher.fetchParty(partyId);
        User user = fetcher.fetchUser(info.userInfo.authenticationName);

        return party.appointDelegate(user, info.start);
    }
}
