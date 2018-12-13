/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Organization extends Party {

    TelephoneNumber getPhone1();

    TelephoneNumber getPhone2();

    PostalAddress getPostalAddress();

    StreetAddress getStreetAddress();

    void setStreetAddress(StreetAddress streetAddress);

    void setPostalAddress(PostalAddress postalAddress);

    void setPhone1(TelephoneNumber telephoneNumber);

    void setPhone2(TelephoneNumber telephoneNumber);

}
