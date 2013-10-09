package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Objects;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

        List<PartyRepresentation> preEdit = new ArrayList<>(party.getCurrentDelegates());
        handleAdditionAndUpdates(preEdit);
        handleRemovals(preEdit);
        return party.getCurrentDelegates();
    }

    private void handleRemovals(List<PartyRepresentation> preEdit) {
        Date now = Bus.getClock().now();
        for (PartyRepresentation partyRepresentation : preEdit) {
            PartyRepresentationInfo delegate = new PartyRepresentationInfo(partyRepresentation);
            delegate.end = now;
            new UpdatePartyRepresentationTransaction(delegate).perform();
        }
    }

    private void handleAdditionAndUpdates(List<PartyRepresentation> preEdit) {
        for (PartyRepresentationInfo delegate : infos.delegates) {
            PartyRepresentation match = pickOriginal(preEdit, delegate);
            if (match == null) { // addition
                new AddDelegateTransaction(id, delegate).perform(); // perform directly since already within transaction context
            } else {
                if (!Objects.equal(delegate.end, match.getInterval().getEnd())) {
                    new UpdatePartyRepresentationTransaction(delegate).perform();
                }
            }
        }
    }

    private PartyRepresentation pickOriginal(List<PartyRepresentation> preEdit, PartyRepresentationInfo delegate) {
        PartyRepresentation match = null;
        for (Iterator<PartyRepresentation> iter = preEdit.iterator(); iter.hasNext();) {
            PartyRepresentation partyRepresentation = iter.next();
            if (delegate.userInfo.id == partyRepresentation.getDelegate().getId()) {
                match = partyRepresentation;
                iter.remove();
                break;
            }
        }
        return match;
    }
}
