package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.rest.util.JsonDateAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement
public class PartyInRoleInfo {

    public long id;
    public long partyId;
    public String roleMRID;
    @XmlJavaTypeAdapter(JsonDateAdapter.class)
    public Date start;
    @XmlJavaTypeAdapter(JsonDateAdapter.class)
    public Date end;
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
