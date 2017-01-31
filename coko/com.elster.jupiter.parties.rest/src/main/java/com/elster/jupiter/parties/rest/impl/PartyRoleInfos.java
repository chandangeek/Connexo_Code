/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyRoleInfos {

    public int total;

    public List<PartyRoleInfo> roles = new ArrayList<>();

    PartyRoleInfos() {
    }

    PartyRoleInfos(PartyRole partyRole) {
        add(partyRole);
    }

    PartyRoleInfos(Iterable<? extends PartyRole> partyRoles) {
        addAll(partyRoles);
    }

    PartyRoleInfo add(PartyRole partyRole) {
        PartyRoleInfo result = new PartyRoleInfo(partyRole);
        roles.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends PartyRole> roles) {
        for (PartyRole each : roles) {
            add(each);
        }
    }

}
