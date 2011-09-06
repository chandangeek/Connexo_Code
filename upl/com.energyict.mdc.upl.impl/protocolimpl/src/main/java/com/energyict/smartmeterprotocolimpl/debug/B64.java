package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.protocolimpl.utils.ProtocolTools;
import sun.misc.BASE64Encoder;

/**
 * Copyrights EnergyICT
 * Date: 25/08/11
 * Time: 15:43
 */
public class B64 {

    private static final String BIN_FW_FILE = "c:\\signed_meter.bin";
    private static final String B64_FW_FILE = "c:\\signed_meter_B64.bin";

    public static void main(String[] args) {
        byte[] rawFirmware = ProtocolTools.readBytesFromFile(BIN_FW_FILE);
        String encoded = new BASE64Encoder().encode(rawFirmware);
        ProtocolTools.writeStringToFile(B64_FW_FILE, encoded, false);
    }

}
