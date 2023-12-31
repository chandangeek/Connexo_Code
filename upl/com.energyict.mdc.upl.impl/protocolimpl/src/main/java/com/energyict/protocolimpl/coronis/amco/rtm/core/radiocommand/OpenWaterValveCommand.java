package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 1-mrt-2011
 * Time: 16:27:00
 */
public class OpenWaterValveCommand extends AbstractRadioCommand {

    protected OpenWaterValveCommand(RTM rtm, PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        success = (data[0] == 0x00);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) 0x00};                             //Writing byte = 0x00 equals opening the water valve.
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ControlWaterValve;
    }
}