package com.energyict.smartmeterprotocolimpl.nta.dsmr40.eict;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.*;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 13:57
 */
public class Dsmr40Protocol extends AbstractSmartNtaProtocol {

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr40Messaging(new Dsmr40MessageExecutor(this));
    }/**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if(this.properties == null){
            this.properties = new Dsmr40Properties();
        }
        return this.properties;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public Dsmr23RegisterFactory getRegisterFactory() {
        if(this.registerFactory == null){
            this.registerFactory = new DSMR40RegisterFactory(this);
        }
        return this.registerFactory;
    }
}
