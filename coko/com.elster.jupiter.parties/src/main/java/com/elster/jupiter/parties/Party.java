/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface Party extends IdentifiedObject {
	long getId();
    ElectronicAddress getElectronicAddress();
    void setMRID(String mRID);
    void setName(String name);
    void setAliasName(String aliasName);
    void setDescription(String description);
    void setElectronicAddress(ElectronicAddress electronicAddress);
    void update();
    void delete();
    long getVersion();
    List<? extends PartyInRole> getPartyInRoles(Range<Instant> range);
    List<? extends PartyInRole> getPartyInRoles(Instant when);
    PartyInRole assumeRole(PartyRole role, Instant start);
    PartyInRole terminateRole(PartyInRole role, Instant end);
    PartyRepresentation appointDelegate(User user, Instant start);
	void adjustRepresentation(PartyRepresentation representation, Range<Instant> newRange);
    void unappointDelegate(User user, Instant end);
    List<PartyRepresentation> getCurrentDelegates();
    Class<? extends Party> getType();
}
