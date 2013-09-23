package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 */
public class UpdateOrganizationTransaction implements Transaction<Organization> {

    private final OrganizationInfo info;

    public UpdateOrganizationTransaction(OrganizationInfo info) {
        this.info = info;
    }

    @Override
    public Organization perform() {
        Organization organization = fetchOrganization();
        validateUpdate(organization);
        return doUpdate(organization);
    }

    private Organization doUpdate(Organization organization) {
        info.update(organization);
        organization.save();
        return organization;
    }

    private void validateUpdate(Organization organization) {
        if (organization.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Organization fetchOrganization() {
        Optional<Party> party = Bus.getPartyService().findParty(info.id);
        if (party.isPresent() && party.get() instanceof Organization) {
            return (Organization) party;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
