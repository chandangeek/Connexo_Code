package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

class Fetcher {

    Party fetchParty(long id) {
        return get(Bus.getPartyService().findParty(id));
    }

    PartyRole fetchRole(String roleMRID) {
        return get(Bus.getPartyService().findPartyRoleByMRID(roleMRID));
    }

    User fetchUser(String authenticationName) {
        return get(Bus.getUserService().findUser(authenticationName));
    }

    private <T> T get(Optional<T> optional) {
        if (!optional.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return optional.get();
    }
}
