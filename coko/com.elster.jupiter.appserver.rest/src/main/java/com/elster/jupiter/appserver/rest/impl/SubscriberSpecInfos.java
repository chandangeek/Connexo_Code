/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.messaging.SubscriberSpec;

import java.util.ArrayList;
import java.util.List;

public class SubscriberSpecInfos {

    public int total;
    public List<SubscriberSpecInfo> subscriberSpecs = new ArrayList<>();

    public SubscriberSpecInfos() {
    }

    public SubscriberSpecInfos(Iterable<SubscriberSpec> subscriberSpecs) {
        addAll(subscriberSpecs);
    }

    public void add(SubscriberSpec subscriberSpec) {
        SubscriberSpecInfo result = SubscriberSpecInfo.of(subscriberSpec);
        subscriberSpecs.add(result);
        total++;
    }

    public void addAll(Iterable<SubscriberSpec> subscriberSpecs) {
        for (SubscriberSpec spec : subscriberSpecs) {
            add(spec);
        }
    }
}



