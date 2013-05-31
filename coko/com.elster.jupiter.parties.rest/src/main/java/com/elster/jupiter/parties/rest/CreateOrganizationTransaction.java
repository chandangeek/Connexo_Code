package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.transaction.Transaction;

public class CreateOrganizationTransaction implements Transaction<Organization> {


    private final OrganizationInfo info;

    public CreateOrganizationTransaction(OrganizationInfo info) {
        this.info = info;
    }

    @Override
    public Organization perform() {
        Organization organization = Bus.getPartyService().newOrganization(info.mRID);
        organization.setName(info.name);
        organization.setAliasName(info.aliasName);
        organization.setDescription(info.description);
        organization.setElectronicAddress(info.electronicAddress);

        organization.setPostalAddress(info.postalAddress);
        organization.setStreetAddress(info.streetAddress);

        organization.save();

        return organization;

    }
}
