package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/06/13
 * Time: 13:18
 */
public class AddDelegateTransaction implements Transaction<List<User>> {

    private final long partyId;
    private final DelegateInfo info;
    private final Fetcher fetcher = new Fetcher();

    public AddDelegateTransaction(long partyId, DelegateInfo info) {
        this.partyId = partyId;
        this.info = info;
    }

    @Override
    public List<User> perform() {
        Party party = fetcher.fetchParty(partyId);
        User user = fetcher.fetchUser(info.delegate.authenticationName);

        party.appointDelegate(user, Bus.getClock().now());
        return party.getCurrentDelegates();
    }

}
