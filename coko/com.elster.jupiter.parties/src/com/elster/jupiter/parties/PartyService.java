package com.elster.jupiter.parties;

import java.util.List;

public interface PartyService {
	Party getParty(String mRID);
	List<Party> getParties();
	PartyRole createRole(String componentName, String mRID, String name, String aliasName, String description);
}
