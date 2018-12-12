/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.transaction.VoidTransaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 */
public class DeleteOrganizationTransaction extends VoidTransaction {

    private final OrganizationInfo info;
    private final Fetcher fetcher;

    @Inject
    public DeleteOrganizationTransaction(OrganizationInfo info, Fetcher fetcher) {
        this.info = info;
        this.fetcher = fetcher;
    }

    @Override
    protected void doPerform() {
        doDelete(fetchOrganization());
    }

    private void doDelete(Organization organization) {
        organization.delete();
    }

    private Organization fetchOrganization() {
        Party party = fetcher.findAndLockParty(this.info);
        if (party instanceof Organization) {
            return (Organization) party;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
