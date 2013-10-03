package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRepresentation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyRepresentationInfos {

    public int total;

    public List<PartyRepresentationInfo> delegates = new ArrayList<>();

    PartyRepresentationInfos() {
    }

    PartyRepresentationInfos(PartyRepresentation partyRepresentation) {
        add(partyRepresentation);
    }

    PartyRepresentationInfos(Iterable<? extends PartyRepresentation> partyRepresentations) {
        addAll(partyRepresentations);
    }

    PartyRepresentationInfo add(PartyRepresentation partyRepresentation) {
        PartyRepresentationInfo result = new PartyRepresentationInfo(partyRepresentation);
        delegates.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends PartyRepresentation> representations) {
        for (PartyRepresentation each : representations) {
            add(each);
        }
    }


}
