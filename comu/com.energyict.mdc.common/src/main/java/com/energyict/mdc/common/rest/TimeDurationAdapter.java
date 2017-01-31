/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.time.TimeDuration;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimeDurationAdapter extends XmlAdapter<String, TimeDuration> {

    @Override
    public TimeDuration unmarshal(String string) throws Exception {
        return new TimeDuration(string);
    }

    @Override
    public String marshal(TimeDuration timeDuration) throws Exception {
        return timeDuration.toString();
    }
}
