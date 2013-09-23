package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.UserInfo;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class UpdatePartyTransaction implements Transaction<Party> {

    private final PartyInfo info;

    public UpdatePartyTransaction(PartyInfo info) {
        this.info = info;
    }

    @Override
    public Party perform() {
        Party party = fetchParty();
        validateUpdate(party);
        return doUpdate(party);
    }

    private Party fetchParty() {
        Optional<Party> party = Bus.getPartyService().findParty(info.id);
        if (party.isPresent()) {
            return party.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private void validateUpdate(Party party) {
        if (party.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Party doUpdate(Party party) {
        info.updateParty(party);
        party.save();
        updateDelegates(party);
        return party;
    }

    private void updateDelegates(Party party) {
        Set<User> current = new LinkedHashSet<>(party.getCurrentDelegates());
        Set<User> target = targetDelegates();
        removeDelegates(party, current, target);
        addDelegates(party, current, target);
    }

    private Set<User> targetDelegates() {
        Set<User> target = new LinkedHashSet<>();
        for (UserInfo userInfo : info.delegates) {
            Optional<User> user = Bus.getUserService().getUser(userInfo.id);
            if (user.isPresent()) {
                target.add(user.get());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return target;
    }

    private void removeDelegates(Party party, Set<User> current, Set<User> target) {
        Set<User> toRemove = new LinkedHashSet<>(current);
        toRemove.removeAll(target);
        for (User user : toRemove) {
            party.unappointDelegate(user, new Date());
        }
    }

    private void addDelegates(Party party, Set<User> current, Set<User> target) {
        Set<User> toAdd = new LinkedHashSet<>(target);
        toAdd.removeAll(current);
        for (User user : toAdd) {
            party.appointDelegate(user, new Date());
        }
    }

}
