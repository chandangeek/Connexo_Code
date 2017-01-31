/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class Fetcher {

    private final PartyService partyService;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    Fetcher(PartyService partyService, UserService userService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.partyService = partyService;
        this.userService = userService;
        this.conflictFactory = conflictFactory;
    }

    Party fetchParty(long id) {
        return get(partyService.findParty(id));
    }

    Party findAndLockParty(PartyInfo info) {
        return partyService.findAndLockPartyByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> partyService.findParty(info.id).map(Party::getVersion).orElse(null))
                        .supplier());
    }

    PartyRepresentation findAndLockPartyRepresentation(PartyRepresentationInfo info) {
        return partyService.findAndLockPartyRepresentationByVersionAndKey(info.version, info.userInfo.authenticationName, info.partyId, info.start.toEpochMilli())
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.userInfo.authenticationName)
                        .withActualVersion(() -> getRepresentation(fetchParty(info.partyId), info.userInfo.authenticationName).map(PartyRepresentation::getVersion).orElse(null))
                        .supplier());
    }

    PartyRole fetchRole(String roleMRID) {
        return get(partyService.findPartyRoleByMRID(roleMRID));
    }

    PartyRole findAndLockPartyRole(PartyRoleInfo info) {
        return partyService.findAndLockRoleByMridAndVersion(info.mRID, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> partyService.findPartyRoleByMRID(info.mRID).map(PartyRole::getVersion).orElse(null))
                        .supplier());
    }

    PartyInRole findAndLockPartyInRole(PartyInRoleInfo info) {
        return partyService.findAndLockPartyInRoleByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.roleMRID)
                        .withActualVersion(() -> partyService.getPartyInRole(info.id).map(PartyInRole::getVersion).orElse(null))
                        .supplier());
    }

    User fetchUser(String authenticationName) {
        return get(userService.findUser(authenticationName));
    }

    Optional<PartyRepresentation> getRepresentation(Party party, String authenticationName) {
        for (PartyRepresentation representation : party.getCurrentDelegates()) {
            if (representation.getDelegate().getName().equals(authenticationName)) {
                return Optional.of(representation);
            }
        }
        return Optional.empty();
    }

    private <T> T get(Optional<T> optional) {
        if (!optional.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return optional.get();
    }
}
