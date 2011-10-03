package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict;

import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 11:58:33
 */
public class WebRTUKP extends AbstractSmartNtaProtocol implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23Messaging(new Dsmr23MessageExecutor(this));
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    @Override
    public String getVersion() {
        return "$Date$";
    }
}
