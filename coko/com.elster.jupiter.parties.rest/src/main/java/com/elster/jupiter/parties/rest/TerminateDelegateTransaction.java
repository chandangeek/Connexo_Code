package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import java.util.List;

public class TerminateDelegateTransaction implements Transaction<List<User>> {

    private final DelegateInfo info;
    private final long partyId;
    private final Fetcher fetcher = new Fetcher();

    public TerminateDelegateTransaction(long partyId, DelegateInfo info) {
        this.partyId = partyId;
        this.info = info;
    }

    @Override
    public List<User> perform() {
        Party party = fetcher.fetchParty(partyId);
        User user = fetcher.fetchUser(info.delegate.authenticationName);
        party.unappointDelegate(user, Bus.getClock().now());
        return party.getCurrentDelegates();
    }

}
