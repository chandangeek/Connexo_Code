package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.mdc.protocol.SerialPortComChannel;

import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

public class KaifaHHUSignon extends IEC1107HHUSignOn {
    public KaifaHHUSignon(SerialPortComChannel comChannel, CommunicationSessionProperties properties) {
        super(comChannel, properties);
    }
}
