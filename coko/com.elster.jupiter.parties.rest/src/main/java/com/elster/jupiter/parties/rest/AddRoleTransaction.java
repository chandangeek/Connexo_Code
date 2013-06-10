package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class AddRoleTransaction implements Transaction<PartyInRole> {

    private final PartyInRoleInfo info;
    private final long partyId;

    public AddRoleTransaction(long partyId, PartyInRoleInfo info) {
        this.partyId = partyId;
        this.info = info;
    }

    @Override
    public PartyInRole perform() {
        return fetchParty().addRole(fetchRole(), interval());
    }

    private Interval interval() {
        return new Interval(info.start, info.end);
    }

    private PartyRole fetchRole() {
        Optional<PartyRole> partyRole = Bus.getPartyService().findPartyRoleByMRID(info.roleMRID);
        if (!partyRole.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return partyRole.get();
    }

    private Party fetchParty() {
        Optional<Party> partyOptional = Bus.getPartyService().findParty(partyId);
        if (!partyOptional.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return partyOptional.get();
    }

}