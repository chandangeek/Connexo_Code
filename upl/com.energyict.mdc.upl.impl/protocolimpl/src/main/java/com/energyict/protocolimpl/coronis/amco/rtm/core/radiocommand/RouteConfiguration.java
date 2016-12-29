package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 13:21:53
 */
public class RouteConfiguration extends AbstractRadioCommand {

    public RouteConfiguration(RTM rtm) {
        super(propertySpecService, rtm);
    }

    private int alarmConfig;
    private int response;

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
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
