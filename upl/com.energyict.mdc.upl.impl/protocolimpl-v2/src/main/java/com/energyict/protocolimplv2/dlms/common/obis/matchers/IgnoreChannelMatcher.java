package com.energyict.protocolimplv2.dlms.common.obis.matchers;


import com.energyict.obis.ObisCode;

public class IgnoreChannelMatcher implements  Matcher<ObisCode>{

    private ObisCode obisCode;
    private final ObisChannel ignoredObisChannel;

    public IgnoreChannelMatcher(ObisCode obisCode, ObisChannel ignoredObisChannel) {
        this.obisCode = obisCode;
        this.ignoredObisChannel = ignoredObisChannel;
    }

    @Override
    public boolean matches(ObisCode o) {
        boolean equals = true;
        for (ObisChannel obisChannel: ObisChannel.values()) {
            if (obisChannel != ignoredObisChannel) {
                equals &= obisChannel.equals(obisCode, o);
            }
        }
        return equals;
    }

    @Override
    public ObisCode map(ObisCode obisCode) {
        return ignoredObisChannel.getDeviceValue(obisCode);
    }

    public ObisChannel getIgnoredObisChannel() {
        return this.ignoredObisChannel;
    }
}
