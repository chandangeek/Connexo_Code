package com.elster.jupiter.parties;

import com.elster.jupiter.util.HasName;

public interface PartyRole extends HasName {
	String getComponentName();
	String getMRID();
	String getAliasName();
	String getDescription();
    long getVersion();

    void setDescription(String description);

    void setName(String name);

    void setAliasName(String aliasName);
}
