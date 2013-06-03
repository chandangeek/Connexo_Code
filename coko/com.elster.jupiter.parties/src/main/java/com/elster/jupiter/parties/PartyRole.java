package com.elster.jupiter.parties;

public interface PartyRole {
	String getComponentName();
	String getMRID();
	String getName();
	String getAliasName();
	String getDescription();
    long getVersion();

    void setDescription(String description);

    void setName(String name);

    void setAliasName(String aliasName);
}
