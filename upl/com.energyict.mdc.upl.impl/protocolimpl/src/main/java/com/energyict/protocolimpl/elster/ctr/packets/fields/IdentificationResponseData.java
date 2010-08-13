package com.energyict.protocolimpl.elster.ctr.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 13-aug-2010
 * Time: 10:34:51
 */
public class IdentificationResponseData extends Data {

    public static final int IDENTIFICATIONCODE_LENGTH = 7;

    private final byte[] identificationCode; //PDR.val

    public IdentificationResponseData(byte[] rawPacket, int offset) {
        int ptr = offset;
        identificationCode = new byte[7];
        for (int i = 0; i < identificationCode.length; i++) {
            identificationCode[i] = rawPacket[ptr++];
        }
        

    }
}
