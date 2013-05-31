package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 31/05/13
 * Time: 14:01
 */
public class DeleteOrganizationTransaction extends VoidTransaction {

    private final OrganizationInfo info;

    public DeleteOrganizationTransaction(OrganizationInfo info) {
        this.info = info;
    }

    @Override
    protected void doPerform() {
        Organization organization = fetchPerson();
        validateDelete(organization);
        doDelete(organization);
    }

    private void doDelete(Organization organization) {
        organization.delete();
    }

    private void validateDelete(Organization organization) {
        if (organization.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Organization fetchPerson() {
        Party party = Bus.getPartyService().findParty(info.id);
        if (!(party instanceof Organization)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return (Organization) party;
    }
}
