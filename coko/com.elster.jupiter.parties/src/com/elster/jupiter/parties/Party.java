package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;

public interface Party {
	long getId();
	String getMRID();
	String getName();
	String getAliasName();
	ElectronicAddress getElectronicAddress();
    String getDescription();

    void setMRID(String mRID);

    void setName(String name);

    void setAliasName(String aliasName);

    void setDescription(String description);

    void setElectronicAddress(ElectronicAddress electronicAddress);

    void save();
}
