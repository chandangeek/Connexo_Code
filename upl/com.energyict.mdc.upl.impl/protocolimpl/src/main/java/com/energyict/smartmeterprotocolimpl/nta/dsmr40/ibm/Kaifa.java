package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.KaifaDsmr40MessageExecutor;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:17
 * Author: khe
 */
public class Kaifa extends E350 {

    @Override
    public String getVersion() {
        return "$Date: 2013-05-02 17:50:49 +0200 (do, 02 mei 2013) $";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = (HHUSignOn) new KaifaHHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                                  //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);            //IEC1107:      300 baud, 7E1
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr40Messaging(new KaifaDsmr40MessageExecutor(this));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            byte[] firmwareVersion = getMeterInfo().getFirmwareVersion().getBytes();
            return ProtocolTools.getHexStringFromBytes(firmwareVersion);
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "Unknown version";
        }
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new KaifaProperties();
        }
        return this.properties;
    }
}