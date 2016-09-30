package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.parties.PartyRepresentation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyRepresentationInfos {

    public int total;
    public List<PartyRepresentationInfo> delegates = new ArrayList<>();

    private NlsService nlsService;

    private PartyRepresentationInfos() {
    }

    private PartyRepresentationInfos(NlsService nlsService) {
        this();
        this.nlsService = nlsService;
    }

    PartyRepresentationInfos(PartyRepresentation partyRepresentation, NlsService nlsService) {
        this(nlsService);
        add(partyRepresentation);
    }

    PartyRepresentationInfos(Iterable<? extends PartyRepresentation> partyRepresentations, NlsService nlsService) {
        this(nlsService);
        addAll(partyRepresentations);
    }

    PartyRepresentationInfo add(PartyRepresentation partyRepresentation) {
        PartyRepresentationInfo result = new PartyRepresentationInfo(this.nlsService, partyRepresentation);
        delegates.add(result);
        total++;
        return result;
    }

    private void addAll(Iterable<? extends PartyRepresentation> representations) {
        for (PartyRepresentation each : representations) {
            add(each);
        }
    }

}