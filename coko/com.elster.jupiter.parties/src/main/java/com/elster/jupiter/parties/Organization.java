package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

public interface Organization extends Party {

    String TYPE_IDENTIFIER = "O";

    TelephoneNumber getPhone1();

    TelephoneNumber getPhone2();

    PostalAddress getPostalAddress();

    StreetAddress getStreetAddress();

    void setStreetAddress(StreetAddress streetAddress);

    void setPostalAddress(PostalAddress postalAddress);

}
