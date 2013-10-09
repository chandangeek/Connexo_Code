package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

public class UpdatePartyRepresentationsTransaction implements Transaction<List<PartyRepresentation>> {

    private final PartyRepresentationInfos infos;
    private final long id;

    public UpdatePartyRepresentationsTransaction(long id, PartyRepresentationInfos infos) {
        this.id = id;
        this.infos = infos;
    }

    @Override
    public List<PartyRepresentation> perform() {
        Optional<Party> found = Bus.getPartyService().findParty(id);
        if (!found.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        Party party = found.get();
        DiffList<PartyRepresentation> representations = new ArrayDiffList<PartyRepresentation>(party.getCurrentDelegates(), Collections.<PartyRepresentation>emptyList());
        return null;
    }
}
