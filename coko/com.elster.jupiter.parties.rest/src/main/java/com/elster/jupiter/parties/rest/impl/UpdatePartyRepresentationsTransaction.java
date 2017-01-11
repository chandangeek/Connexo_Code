package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.rest.UserInfoFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

class UpdatePartyRepresentationsTransaction implements Transaction<List<? extends PartyRepresentation>> {

    private final PartyRepresentationInfos infos;
    private final long id;
    private final Clock clock;
    private final NlsService nlsService;
    private final Fetcher fetcher;
    private final UserInfoFactory userInfoFactory;

    @Inject
    UpdatePartyRepresentationsTransaction(long id, PartyRepresentationInfos infos, Clock clock, NlsService nlsService, Fetcher fetcher, UserInfoFactory userInfoFactory) {
        this.id = id;
        this.infos = infos;
        this.clock = clock;
        this.nlsService = nlsService;
        this.fetcher = fetcher;
        this.userInfoFactory = userInfoFactory;
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
            PartyRepresentationInfo delegate = new PartyRepresentationInfo(this.nlsService, partyRepresentation, userInfoFactory);
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
