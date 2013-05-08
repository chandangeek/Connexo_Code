package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

public interface Organization extends Party {
	TelephoneNumber getPhone1();
	TelephoneNumber getPhone2();
	PostalAddress getPostalAddress();
	StreetAddress getStreetAddress();
}
