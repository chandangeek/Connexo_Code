package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.IdentifiedObject;

public interface PartyRole extends IdentifiedObject {
	String getComponentName();
    long getVersion();

    void setDescription(String description);

    void setName(String name);

    void setAliasName(String aliasName);
}
