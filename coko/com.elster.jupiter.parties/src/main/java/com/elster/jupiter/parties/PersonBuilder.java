/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

@ProviderType
public interface PersonBuilder {

    PersonBuilder setMRID(String mRID);

    PersonBuilder setName(String name);

    PersonBuilder setAliasName(String aliasName);

    PersonBuilder setDescription(String description);

    PersonBuilder setElectronicAddress(ElectronicAddress electronicAddress);

    PersonBuilder setLandLinePhone(TelephoneNumber telephoneNumber);

    PersonBuilder setMiddleName(String mName);

    PersonBuilder setMobilePhone(TelephoneNumber telephoneNumber);

    PersonBuilder setPrefix(String prefix);

    PersonBuilder setSpecialNeed(String specialNeed);

    PersonBuilder setSuffix(String suffix);

    PersonBuilder setFirstName(String firstName);

    PersonBuilder setLastName(String lastName);

    Person create();

}
