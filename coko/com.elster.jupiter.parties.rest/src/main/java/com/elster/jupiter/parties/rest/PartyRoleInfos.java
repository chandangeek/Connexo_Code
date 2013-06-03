package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyRole;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PartyRoleInfos {

    public int total;

    public List<PartyRoleInfo> roles = new ArrayList<>();

    PartyRoleInfos() {
    }

    PartyRoleInfos(PartyRole partyRole) {
        add(partyRole);
    }

    PartyRoleInfos(List<PartyRole> partyRoles) {
        addAll(partyRoles);
    }

    PartyRoleInfo add(PartyRole partyRole) {
        PartyRoleInfo result = new PartyRoleInfo(partyRole);
        roles.add(result);
        total++;
        return result;
    }

    void addAll(List<PartyRole> roles) {
        for (PartyRole each : roles) {
            add(each);
        }
    }

}
