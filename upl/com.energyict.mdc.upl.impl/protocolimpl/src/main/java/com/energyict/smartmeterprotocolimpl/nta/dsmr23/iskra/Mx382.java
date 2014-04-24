package com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra;

import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 11:59:24
 */
public class Mx382 extends AbstractSmartNtaProtocol {

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23Messaging(new Dsmr23MessageExecutor(this));
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                                  //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);      //IEC1107:      300 baud, 7E1
        getDlmsSession().getDLMSConnection().setSNRMType(1);                      //Uses a specific parameter length for the HDLC signon (SNRM request)
    }

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Mx382 NTA DSMR2.3";
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
