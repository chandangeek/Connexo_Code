package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 31/05/13
 * Time: 13:45
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
        Party party = Bus.getPartyService().findParty(info.id);
        if (!(party instanceof Organization)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Organization) party;
    }

}
