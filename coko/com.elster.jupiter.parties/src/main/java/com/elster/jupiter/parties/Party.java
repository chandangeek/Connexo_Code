package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.util.time.Interval;

import java.util.List;

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

    void delete();

    long getVersion();

    List<PartyInRole> getPartyInRoles();

    PartyInRole addRole(PartyRole role, Interval interval);


}
