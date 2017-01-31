/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;

public class CreateOrganizationTransaction implements Transaction<Organization> {

    private final OrganizationInfo info;
    private final PartyService partyService;

    @Inject
    public CreateOrganizationTransaction(OrganizationInfo info, PartyService partyService) {
        this.info = info;
        this.partyService = partyService;
    }

    @Override
    public Organization perform() {

        return partyService.newOrganization(info.mRID)
                .setName(info.name)
                .setAliasName(info.aliasName)
                .setDescription(info.description)
                .setElectronicAddress(info.electronicAddress)
                .setPostalAddress(info.postalAddress)
                .setStreetAddress(info.streetAddress)
                .setPhone1(info.phone1)
                .setPhone2(info.phone2)
                .create();

    }
}
