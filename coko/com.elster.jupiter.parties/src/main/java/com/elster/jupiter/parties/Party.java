package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasName;

import java.util.Date;
import java.util.List;

public interface Party extends HasName {

    long getId();

    String getMRID();

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

    PartyInRole assumeRole(PartyRole role, Date start);

    PartyInRole terminateRole(PartyInRole role, Date end);

    void appointDelegate(User user, Date start);

    void unappointDelegate(User user, Date end);

    List<User> getCurrentDelegates();
}
