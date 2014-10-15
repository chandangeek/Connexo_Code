package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.rest.util.JsonInstantAdapter;
import com.elster.jupiter.users.rest.UserInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.Instant;

@XmlRootElement
public class PartyRepresentationInfo {
    public long partyId;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant start;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
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
