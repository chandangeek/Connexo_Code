/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Status;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Arrays;

public class StatusAdapter extends XmlAdapter<String, Status> {

    public StatusAdapter() {
    }

    @Override
    public Status unmarshal(String jsonValue) throws Exception {
        return Arrays.stream(Status.values())
                .filter(status -> status.name().equals(jsonValue))
                .findAny()
                .orElse(null);
    }

    @Override
    public String marshal(Status status) throws Exception {
        return status.name();
    }
}