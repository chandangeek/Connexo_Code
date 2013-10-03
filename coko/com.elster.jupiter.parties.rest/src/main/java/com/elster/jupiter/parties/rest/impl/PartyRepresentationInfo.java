package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.rest.util.JsonDateAdapter;
import com.elster.jupiter.users.rest.UserInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement
public class PartyRepresentationInfo {
    public long partyId;
    @XmlJavaTypeAdapter(JsonDateAdapter.class)
    public Date start;
    @XmlJavaTypeAdapter(JsonDateAdapter.class)
    public Date end;
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
