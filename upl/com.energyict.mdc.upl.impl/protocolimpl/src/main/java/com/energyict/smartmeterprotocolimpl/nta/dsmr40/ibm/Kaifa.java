package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;

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
        return "$Date$";
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
