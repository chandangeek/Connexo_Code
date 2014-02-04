package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TimeDuration;
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
