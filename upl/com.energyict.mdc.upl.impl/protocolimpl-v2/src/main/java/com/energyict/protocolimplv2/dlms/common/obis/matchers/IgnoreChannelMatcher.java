package com.energyict.protocolimplv2.dlms.common.obis.matchers;


import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IgnoreChannelMatcher implements Matcher<ObisCode> {

    private ObisCode obisCode;
    private final ObisChannel ignoredObisChannel;
    private final List<Integer> exceptionValues;

    public IgnoreChannelMatcher(ObisCode obisCode, ObisChannel ignoredObisChannel) {
        this.obisCode = obisCode;
        this.ignoredObisChannel = ignoredObisChannel;
        this.exceptionValues = new ArrayList<>();
    }

    public IgnoreChannelMatcher(ObisCode obisCode, ObisChannel ignoredObisChannel, Integer... exceptionValues) {
        this.obisCode = obisCode;
        this.ignoredObisChannel = ignoredObisChannel;
        this.exceptionValues = Arrays.asList(exceptionValues);
    }

    @Override
    public boolean matches(ObisCode o) {
        for (ObisChannel obisChannel : ObisChannel.values()) {
            if (obisChannel != ignoredObisChannel) {
                if(!obisChannel.equals(obisCode, o)){
                    return false;
                }
            } else {
                if (exceptionValues.contains(obisChannel.getValue(o))){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ObisCode map(ObisCode obisCode) {
        return exceptionValues.contains(ignoredObisChannel.getValue(obisCode)) ? obisCode : ignoredObisChannel.getDeviceValue(obisCode);
    }

    public ObisChannel getIgnoredObisChannel() {
        return this.ignoredObisChannel;
    }
}
