package com.energyict.smartmeterprotocolimpl.debug;

import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 25/08/11
 * Time: 15:43
 */
public class B64 {

    private static final String BIN_FW_FILE = "/home/jme/fileserver/Install/EnergyICT test release/Firmware/WebRTU AM110-R/R3.03.03(P37)/AM110R_V3_03_03_P37.bin";
    private static final String B64_FW_FILE = "/home/jme/fileserver/Install/EnergyICT test release/Firmware/WebRTU AM110-R/R3.03.03(P37)/AM110R_V3_03_03_P37_B64.bin";

    public static void main(String[] args) throws IOException {
        byte[] rawFirmware = ProtocolTools.readBytesFromFile(BIN_FW_FILE);
        String encoded = new Base64EncoderDecoder().encode(rawFirmware);
        ProtocolTools.writeStringToFile(B64_FW_FILE, encoded, false);
    }

}
