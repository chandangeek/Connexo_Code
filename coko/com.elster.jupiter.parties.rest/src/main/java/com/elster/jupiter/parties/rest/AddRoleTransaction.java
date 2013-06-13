package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.Transaction;


public class AddRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final long partyId;
    private final Fetcher fetcher = new Fetcher();

    public AddRoleTransaction(long partyId, PartyInRoleInfo info) {
        this.partyId = partyId;
        this.info = info;
    }

    @Override
    public PartyInRole perform() {
        Party party = fetcher.fetchParty(partyId);
        PartyRole role = fetcher.fetchRole(info.roleMRID);

        return party.assumeRole(role, info.start);
    }

}