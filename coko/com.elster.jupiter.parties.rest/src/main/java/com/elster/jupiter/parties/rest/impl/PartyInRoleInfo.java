/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyInRole;

import javax.xml.bind.annotation.XmlRootElement;

import java.time.Instant;

@XmlRootElement
public class PartyInRoleInfo {

    public long id;
    public long partyId;
    public String roleMRID;
    public Instant start;
    public Instant end;
    public PartyRoleInfo partyRoleInfo;
    public long version;
    public PartyInfo parent;

    PartyInRoleInfo() {
    }

    public PartyInRoleInfo(PartyInRole partyInRole) {
        partyRoleInfo = new PartyRoleInfo(partyInRole.getRole());
        partyId = partyInRole.getParty().getId();
        id = partyInRole.getId();
        start = partyInRole.getInterval().getStart();
        end = partyInRole.getInterval().getEnd();
        version = partyInRole.getVersion();
        parent = new PartyInfo(partyInRole.getParty());
    }

}
