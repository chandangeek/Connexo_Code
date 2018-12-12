package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 4-mei-2011
 * Time: 17:29:20
 */
public class RSSILevel extends AbstractRadioCommand {

    private int rssiLevel;
    private static final double MAX = 0x20;

    protected RSSILevel(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public RSSILevel(PropertySpecService propertySpecService, RTM rtm, int qos, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.rssiLevel = qos;
    }

    /*
    A percentage representing the saturation. 100% = full saturation = 48 dB
    */
    public double getRssiLevel() {
        return (((double) rssiLevel) / MAX) * 100;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        rssiLevel = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.RssiLevel;
    }
}