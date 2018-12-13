/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 */
public class UpdateOrganizationTransaction implements Transaction<Organization> {

    private final OrganizationInfo info;
    private final Fetcher fetcher;

    @Inject
    public UpdateOrganizationTransaction(OrganizationInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    public Organization perform() {
        return doUpdate(fetchOrganization());
    }

    private Organization doUpdate(Organization organization) {
        info.update(organization);
        organization.update();
        return organization;
    }

    private Organization fetchOrganization() {
        Party party = fetcher.findAndLockParty(this.info);
        if (party instanceof Organization) {
            return (Organization) party;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

}
