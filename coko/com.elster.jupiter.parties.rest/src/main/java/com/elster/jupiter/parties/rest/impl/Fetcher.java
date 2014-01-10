package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class Fetcher {

    private final PartyService partyService;
    private final UserService userService;

    Fetcher(PartyService partyService, UserService userService) {
        this.partyService = partyService;
        this.userService = userService;
    }

    Party fetchParty(long id) {
        return get(partyService.findParty(id));
    }

    PartyRole fetchRole(String roleMRID) {
        return get(partyService.findPartyRoleByMRID(roleMRID));
    }

    User fetchUser(String authenticationName) {
        return get(userService.findUser(authenticationName));
    }

    private <T> T get(Optional<T> optional) {
        if (!optional.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return optional.get();
    }
}
