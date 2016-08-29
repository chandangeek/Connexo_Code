package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * This is the root class of all data passed to and from the device
 * Responsible for gemerating the CRC
 *
 * @author James Fox
 */
public abstract class Data {

    /**
     * Creates a byte array representation of the data suitable for packaging
     * into a frame
     *
     * @param includeEtx if true, ETX is included at the end, if false not
     * @return a byte array representation of the data (either with or without ETX)
     * @throws IOException
     */
    public abstract byte[] toByteArray(boolean includeEtx) throws IOException;

    /**
     * Generates the CRC for this data (including ETX)
     * @return a four-byte byte array representing the CRC for data with ETX
     * @throws IOException
     */
    public byte[] generateCRC() throws IOException {
        int crc = CRCGenerator.calcCCITTCRC(toByteArray(true));
        byte[] crcBytes = ProtocolTools.getBytesFromInt(crc, 2);
        String crcASCII = ProtocolTools.getHexStringFromBytes(crcBytes, "");
        return crcASCII.getBytes();
    }
}
