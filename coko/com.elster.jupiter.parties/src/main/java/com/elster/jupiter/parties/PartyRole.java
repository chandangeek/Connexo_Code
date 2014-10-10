package com.elster.jupiter.parties;

import java.time.Instant;
import java.util.List;

import com.elster.jupiter.cbo.IdentifiedObject;

public interface PartyRole extends IdentifiedObject {
	String getComponentName();
    long getVersion();

    void setDescription(String description);

    void setName(String name);

    void setAliasName(String aliasName);
    
    List<Party> getParties();
    List<Party> getParties(Instant effectiveInstant);
}
