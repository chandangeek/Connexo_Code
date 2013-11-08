package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.dlms.DlmsSession;
import com.energyict.protocolimpl.dlms.g3.G3Properties;

/**
 * Copyrights EnergyICT
 * Date: 26/11/12
 * Time: 14:00
 * Author: khe
 */
public class G3MessagingSagemCom extends G3Messaging {

    public G3MessagingSagemCom(final DlmsSession session, G3Properties properties) {
        super(session, properties);
    }

    @Override
    protected String getImageIdentifier(byte[] firmwareBytes) {
        return "newImage";  //Doesn't matter for this meter
    }

    @Override
    protected boolean shouldUseSecondInterface(String trackingId) {
        return false;   //There is only one interface available for the SagemCom meter
    }
}