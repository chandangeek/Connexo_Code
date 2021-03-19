package com.energyict.protocolimplv2.dlms.actaris.sl7000.obis.matchers;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;

public class ChannelValueNotMatching implements Matcher<ObisCode> {

    private final ObisChannel obisChannel;
    private final int value;

    public ChannelValueNotMatching(ObisChannel obisChannel, int value) {
        this.obisChannel = obisChannel;
        this.value = value;
    }

    @Override
    public boolean matches(ObisCode o) {
        return obisChannel.getValue(o) != value;
    }

    @Override
    public ObisCode map(ObisCode obisCode) {
        return obisCode;
    }

    public int getIgnoredValue(ObisCode o) {
        return obisChannel.getValue(o);
    }

}
