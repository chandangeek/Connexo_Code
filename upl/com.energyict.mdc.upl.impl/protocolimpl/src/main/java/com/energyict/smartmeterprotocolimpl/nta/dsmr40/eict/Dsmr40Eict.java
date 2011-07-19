package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eict;

import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Messaging;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 13:57
 */
public class Dsmr40Eict extends AbstractSmartNtaProtocol {

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23Messaging(new Dsmr23MessageExecutor(this));
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

}
