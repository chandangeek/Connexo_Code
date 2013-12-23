package com.elster.jupiter.parties;

import com.elster.jupiter.domain.util.Query;
import com.google.common.base.Optional;

import java.util.List;

public interface PartyService {
	static String COMPONENTNAME = "PRT";
    Optional<Party> getParty(String mRID);
    List<Party> getParties();
    Query<Party> getPartyQuery();
    Person newPerson(String firstName, String lastName);
	Optional<Party> getParty(long id);
    Optional<Party> findParty(long id);
    Organization newOrganization(String mRID);
    PartyRole createRole(String componentName, String mRID, String name, String aliasName, String description);
    List<PartyRole> getPartyRoles();
    Optional<PartyRole> findPartyRoleByMRID(String mRID);
    void deletePartyRole(PartyRole partyRole);
    void updateRole(PartyRole partyRole);
    void updateRepresentation(PartyRepresentation representation);
	Optional<PartyRole> getRole(String mRID);
}
