package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.parties.PartyRepresentation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyRepresentationInfos {

    public int total;
    private Thesaurus thesaurus;

    public List<PartyRepresentationInfo> delegates = new ArrayList<>();

    PartyRepresentationInfos() {
    }

    private PartyRepresentationInfos(Thesaurus thesaurus) {
        this();
        this.thesaurus = thesaurus;
    }

    PartyRepresentationInfos(PartyRepresentation partyRepresentation, Thesaurus thesaurus) {
        this(thesaurus);
        add(partyRepresentation);
    }

    PartyRepresentationInfos(Iterable<? extends PartyRepresentation> partyRepresentations, Thesaurus thesaurus) {
        this(thesaurus);
        addAll(partyRepresentations);
    }

    PartyRepresentationInfo add(PartyRepresentation partyRepresentation) {
        PartyRepresentationInfo result = new PartyRepresentationInfo(this.thesaurus, partyRepresentation);
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