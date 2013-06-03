package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyInRole;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
public class PartyInRoleInfo {

    public long id;
    public String roleMRID;
    public Date start;
    public Date end;
    public PartyRoleInfo partyRoleInfo;

    PartyInRoleInfo() {
    }

    public PartyInRoleInfo(PartyInRole partyInRole) {
        partyRoleInfo = new PartyRoleInfo(partyInRole.getRole());
        id = partyInRole.getId();
        start = partyInRole.getInterval().getStart();
        end = partyInRole.getInterval().getEnd();
    }
}
