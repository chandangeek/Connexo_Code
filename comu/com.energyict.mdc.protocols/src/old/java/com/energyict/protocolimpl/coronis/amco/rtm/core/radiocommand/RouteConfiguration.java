/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class RouteConfiguration extends AbstractRadioCommand {

    public RouteConfiguration(RTM rtm) {
        super(rtm);
    }

    private int alarmConfig;
    private int response;

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        response = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) alarmConfig};
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.RouteConfiguration;
    }

    public int getResponse() {
        return response;
    }
}
