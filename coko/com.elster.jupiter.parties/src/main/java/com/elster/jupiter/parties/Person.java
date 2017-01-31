/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.TelephoneNumber;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Person extends Party {

    String getFirstName();

    TelephoneNumber getLandLinePhone();

    String getLastName();

    String getMiddleName();

    TelephoneNumber getMobilePhone();

    String getPrefix();

    String getSpecialNeed();

    String getSuffix();

    void setLandLinePhone(TelephoneNumber telephoneNumber);

    void setMiddleName(String mName);

    void setMobilePhone(TelephoneNumber telephoneNumber);

    void setPrefix(String prefix);

    void setSpecialNeed(String specialNeed);

    void setSuffix(String suffix);

    void setFirstName(String firstName);

    void setLastName(String lastName);
}
