/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import com.elster.jupiter.domain.util.Query;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface PartyService {
	String COMPONENTNAME = "PRT";
    Optional<Party> getParty(String mRID);
    List<Party> getParties();
    Query<Party> getPartyQuery();
    Query<Organization> getOrganizationQuery();
    Query<Person> getPersonQuery();
    PersonBuilder newPerson(String firstName, String lastName);
	Optional<Party> getParty(long id);
    Optional<Party> findParty(long id);
    Optional<Party> findAndLockPartyByIdAndVersion(long id, long version);
    OrganizationBuilder newOrganization(String mRID);
    PartyRole createRole(String componentName, String mRID, String name, String aliasName, String description);
    List<PartyRole> getPartyRoles();
    Optional<PartyRole> findPartyRoleByMRID(String mRID);
    void deletePartyRole(PartyRole partyRole);
    void updateRole(PartyRole partyRole);
    void updateRepresentation(PartyRepresentation representation);
	Optional<PartyRole> getRole(String mRID);
	Optional<PartyRole> findAndLockRoleByMridAndVersion(String mRID, long version);
    Optional<PartyInRole> getPartyInRole(long id);
	Optional<PartyInRole> findAndLockPartyInRoleByIdAndVersion(long id, long version);
	Optional<PartyRepresentation> findAndLockPartyRepresentationByVersionAndKey(long version, Object... key);
}
