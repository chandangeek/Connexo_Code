package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

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
    List<? extends PartyInRole> getPartyInRoles();
    PartyInRole assumeRole(PartyRole role, Date start);
    PartyInRole terminateRole(PartyInRole role, Date end);
    PartyRepresentation appointDelegate(User user, Date start);
	void adjustRepresentation(PartyRepresentation representation, Interval newInterval);
    void unappointDelegate(User user, Date end);
    List<? extends PartyRepresentation> getCurrentDelegates();
    Class<? extends Party> getType();
}
