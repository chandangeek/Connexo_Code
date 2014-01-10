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
        Organization organization = partyService.newOrganization(info.mRID);
        organization.setName(info.name);
        organization.setAliasName(info.aliasName);
        organization.setDescription(info.description);
        organization.setElectronicAddress(info.electronicAddress);

        organization.setPostalAddress(info.postalAddress);
        organization.setStreetAddress(info.streetAddress);
        organization.setPhone1(info.phone1);
        organization.setPhone2(info.phone2);

        organization.save();

        return organization;

    }
}
