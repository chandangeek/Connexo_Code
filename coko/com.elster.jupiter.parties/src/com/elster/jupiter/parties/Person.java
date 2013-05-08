package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.TelephoneNumber;

public interface Person extends Party {
	String getFirstName();
	String getLastName();
	String getMiddleName();
	String getPrefix();
	String getSuffix();
	TelephoneNumber getLandLinePhone();
	TelephoneNumber getMobilePhone();	
}
