package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 13:21:53
 */
public class RouteConfiguration extends AbstractRadioCommand {

    public RouteConfiguration(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
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
