package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyInRole;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyInRoleInfos {

    public int total;

    public List<PartyInRoleInfo> roles = new ArrayList<>();

    PartyInRoleInfos() {
    }

    PartyInRoleInfos(PartyInRole partyInRole) {
        add(partyInRole);
    }

    PartyInRoleInfos(List<PartyInRole> partyInRoles) {
        addAll(partyInRoles);
    }

    PartyInRoleInfo add(PartyInRole partyInRole) {
        PartyInRoleInfo result = new PartyInRoleInfo(partyInRole);
        roles.add(result);
        total++;
        return result;
    }

    void addAll(List<PartyInRole> roles) {
        for (PartyInRole each : roles) {
            add(each);
        }
    }


}
