package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 */
public class UpdateOrganizationTransaction implements Transaction<Organization> {

    private final OrganizationInfo info;
    private final PartyService partyService;

    @Inject
    public UpdateOrganizationTransaction(OrganizationInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
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
        Optional<Party> party = partyService.findParty(info.id);
        if (party.isPresent() && party.get() instanceof Organization) {
            return (Organization) party;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
