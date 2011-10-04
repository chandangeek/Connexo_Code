package com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.messaging.LoadProfileRegisterMessaging;
import com.energyict.protocol.messaging.PartialLoadProfileMessaging;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 11:59:24
 */
public class Mx382 extends AbstractSmartNtaProtocol implements PartialLoadProfileMessaging, LoadProfileRegisterMessaging {

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
