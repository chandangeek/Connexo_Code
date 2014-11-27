package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.rest.UserInfo;

import javax.xml.bind.annotation.XmlRootElement;

import java.time.Instant;

@XmlRootElement
public class PartyRepresentationInfo {
    public long partyId;
    public Instant start;
    public Instant end;
    public UserInfo userInfo;

    PartyRepresentationInfo() {

    }

    public PartyRepresentationInfo(PartyRepresentation partyRepresentation) {
        partyId = partyRepresentation.getParty().getId();
        start = partyRepresentation.getInterval().getStart();
        end = partyRepresentation.getInterval().getEnd();
        userInfo = new UserInfo(partyRepresentation.getDelegate());
    }
}
