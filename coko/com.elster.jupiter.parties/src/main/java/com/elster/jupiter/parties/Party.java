package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.users.User;

import java.util.Date;
import java.util.List;

public interface Party extends IdentifiedObject {

    long getId();

    ElectronicAddress getElectronicAddress();

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

    PartyRepresentation appointDelegate(User user, Date start);

    void unappointDelegate(User user, Date end);

    List<PartyRepresentation> getCurrentDelegates();

    String getType();
}
