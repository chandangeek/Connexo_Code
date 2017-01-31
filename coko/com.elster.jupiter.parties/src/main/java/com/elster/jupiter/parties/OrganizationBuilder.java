/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

@ProviderType
public interface OrganizationBuilder {

    OrganizationBuilder setMRID(String mRID);

    OrganizationBuilder setName(String name);

    OrganizationBuilder setAliasName(String aliasName);

    OrganizationBuilder setDescription(String description);

    OrganizationBuilder setElectronicAddress(ElectronicAddress electronicAddress);

    OrganizationBuilder setStreetAddress(StreetAddress streetAddress);

    OrganizationBuilder setPostalAddress(PostalAddress postalAddress);

    OrganizationBuilder setPhone1(TelephoneNumber telephoneNumber);

    OrganizationBuilder setPhone2(TelephoneNumber telephoneNumber);

    Organization create();
}
