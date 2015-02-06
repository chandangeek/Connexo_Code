package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;


public class SubscriberSpecInfos {

    public int total;
    public List<SubscriberSpecInfo> subscriberSpecs = new ArrayList<>();

    public SubscriberSpecInfos() {
    }

    public SubscriberSpecInfos(Iterable<SubscriberSpec> subscriberSpecs, Thesaurus thesaurus) {
        addAll(subscriberSpecs, thesaurus);
    }

    public void add(SubscriberSpec subscriberSpec, Thesaurus thesaurus) {
        SubscriberSpecInfo result = SubscriberSpecInfo.of(subscriberSpec, thesaurus);
        subscriberSpecs.add(result);
        total++;
    }

    public void addAll(Iterable<SubscriberSpec> subscriberSpecs, Thesaurus thesaurus) {
        for (SubscriberSpec spec : subscriberSpecs) {
            add(spec, thesaurus);
        }
    }
}



