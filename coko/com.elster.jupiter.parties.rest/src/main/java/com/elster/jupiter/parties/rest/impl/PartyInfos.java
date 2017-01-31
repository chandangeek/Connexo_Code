/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.Party;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class PartyInfos {

    public int total;

    public List<PartyInfo> parties = new ArrayList<>();

    PartyInfos() {
    }

    PartyInfos(Party party) {
        add(party);
    }

    PartyInfos(Iterable<? extends Party> parties) {
        addAll(parties);
    }

    PartyInfo add(Party party) {
        PartyInfo result = new PartyInfo(party);
        parties.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Party> partys) {
        for (Party each : partys) {
            add(each);
        }
    }

}
