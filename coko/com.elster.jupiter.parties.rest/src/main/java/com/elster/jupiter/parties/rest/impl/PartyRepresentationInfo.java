package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.NlsService;
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
    public PartyInfo parent;
    public long version;

    PartyRepresentationInfo() {
    }

    public PartyRepresentationInfo(NlsService nlsService, PartyRepresentation partyRepresentation) {
        this();
        partyId = partyRepresentation.getParty().getId();
        start = partyRepresentation.getInterval().getStart();
        end = partyRepresentation.getInterval().getEnd();
        userInfo = new UserInfo(nlsService, partyRepresentation.getDelegate());
        parent = new PartyInfo(partyRepresentation.getParty());
        version = partyRepresentation.getVersion();
    }

}