/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.rest.UserInfoFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyRepresentationInfos {

    public int total;
    public List<PartyRepresentationInfo> delegates = new ArrayList<>();

    private NlsService nlsService;
    private UserInfoFactory userInfoFactory;

    private PartyRepresentationInfos() {
    }

    private PartyRepresentationInfos(NlsService nlsService, UserInfoFactory userInfoFactory) {
        this();
        this.nlsService = nlsService;
        this.userInfoFactory = userInfoFactory;
    }

    PartyRepresentationInfos(PartyRepresentation partyRepresentation, NlsService nlsService, UserInfoFactory userInfoFactory) {
        this(nlsService, userInfoFactory);
        add(partyRepresentation);
    }

    PartyRepresentationInfos(Iterable<? extends PartyRepresentation> partyRepresentations, NlsService nlsService, UserInfoFactory userInfoFactory) {
        this(nlsService, userInfoFactory);
        addAll(partyRepresentations);
    }

    PartyRepresentationInfo add(PartyRepresentation partyRepresentation) {
        PartyRepresentationInfo result = new PartyRepresentationInfo(this.nlsService, partyRepresentation, userInfoFactory);
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