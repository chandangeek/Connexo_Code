package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdatePartyRepresentationsTransaction implements Transaction<List<? extends PartyRepresentation>> {

    private final PartyRepresentationInfos infos;
    private final long id;
    private final PartyService partyService;
    private final Clock clock;
    private final UserService userService;

    @Inject
    public UpdatePartyRepresentationsTransaction(long id, PartyRepresentationInfos infos, PartyService partyService, Clock clock, UserService userService) {
        this.id = id;
        this.infos = infos;
        this.partyService = partyService;
        this.clock = clock;
        this.userService = userService;
    }

    @Override
    public List<? extends PartyRepresentation> perform() {
        Optional<Party> found = partyService.findParty(id);
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
        Instant now = clock.instant();
        for (PartyRepresentation partyRepresentation : preEdit) {
            PartyRepresentationInfo delegate = new PartyRepresentationInfo(partyRepresentation);
            delegate.end = now;
            new UpdatePartyRepresentationTransaction(userService, partyService, delegate).perform();
        }
    }

    private void handleAdditionAndUpdates(List<PartyRepresentation> preEdit) {
        for (PartyRepresentationInfo delegate : infos.delegates) {
            PartyRepresentation match = pickOriginal(preEdit, delegate);
            if (match == null) { // addition
                new AddDelegateTransaction(id, delegate, userService, partyService).perform(); // perform directly since already within transaction context
            } else {
                if (!Objects.equals(delegate.end, match.getInterval().getEnd())) {
                    new UpdatePartyRepresentationTransaction(userService, partyService, delegate).perform();
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
