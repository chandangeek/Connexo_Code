package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.rest.util.JsonInstantAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.Instant;

@XmlRootElement
public class PartyInRoleInfo {

    public long id;
    public long partyId;
    public String roleMRID;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant start;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant end;
    public PartyRoleInfo partyRoleInfo;
    public long version;

    PartyInRoleInfo() {
    }

    public PartyInRoleInfo(PartyInRole partyInRole) {
        partyRoleInfo = new PartyRoleInfo(partyInRole.getRole());
        partyId = partyInRole.getParty().getId();
        id = partyInRole.getId();
        start = partyInRole.getInterval().getStart();
        end = partyInRole.getInterval().getEnd();
        version = partyInRole.getVersion();
    }

}
