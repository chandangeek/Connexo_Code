package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdatePartyRepresentationsTransaction implements Transaction<List<? extends PartyRepresentation>> {

    private final PartyRepresentationInfos infos;
    private final long id;
    private final Clock clock;
    private final Fetcher fetcher;
    private final PartyService partyService;

    @Inject
    public UpdatePartyRepresentationsTransaction(long id, PartyRepresentationInfos infos, PartyService partyService, Clock clock, Fetcher fetcher) {
        this.id = id;
        this.infos = infos;
        this.clock = clock;
        this.fetcher = fetcher;
        this.partyService = partyService;
    }

    @Override
    public List<? extends PartyRepresentation> perform() {
        Party party = fetcher.fetchParty(id);

        List<PartyRepresentation> preEdit = new ArrayList<>(party.getCurrentDelegates());
        handleAdditionAndUpdates(preEdit);
        handleRemovals(preEdit);
        return party.getCurrentDelegates();
    }

    private void handleRemovals(List<PartyRepresentation> preEdit) {
        Instant now = clock.instant();
        for (PartyRepresentation partyRepresentation : preEdit) {
            PartyRepresentationInfo delegate = new PartyRepresentationInfo(partyRepresentation);
            delegate.end = now;
            new UpdatePartyRepresentationTransaction(delegate, fetcher).perform();
        }
    }

    private void handleAdditionAndUpdates(List<PartyRepresentation> preEdit) {
        for (PartyRepresentationInfo delegate : infos.delegates) {
            PartyRepresentation match = pickOriginal(preEdit, delegate);
            if (match == null) { // addition
                new AddDelegateTransaction(id, delegate, fetcher).perform(); // perform directly since already within transaction context
            } else {
                if (!Objects.equals(delegate.end, match.getInterval().getEnd())) {
                    new UpdatePartyRepresentationTransaction(delegate, fetcher).perform();
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
