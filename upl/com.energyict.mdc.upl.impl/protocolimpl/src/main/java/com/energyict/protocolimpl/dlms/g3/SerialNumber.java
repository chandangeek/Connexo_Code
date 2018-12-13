package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.upl.ProtocolException;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 6/27/12
 * Time: 3:00 PM
 */
public class SerialNumber {

    private static int LENGTH = 16;

    private final String manufacturerIdentifier;
    private final int numberOfPhases;
    private final String euridisADS;

    public SerialNumber(String manufacturerIdentifier, int numberOfPhases, String euridisADS) {
        this.manufacturerIdentifier = manufacturerIdentifier;
        this.numberOfPhases = numberOfPhases;
        this.euridisADS = euridisADS;
    }

    public String getManufacturerIdentifier() {
        return manufacturerIdentifier;
    }

    public int getNumberOfPhases() {
        return numberOfPhases;
    }

    public String getEuridisADS() {
        return euridisADS;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SerialNumber");
        sb.append("{manufacturerIdentifier='").append(manufacturerIdentifier).append('\'');
        sb.append(", numberOfPhases=").append(numberOfPhases);
        sb.append(", euridisADS='").append(euridisADS).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final SerialNumber fromBytes(byte[] rawBytes) throws ProtocolException {
        if (rawBytes == null) {
            throw new ProtocolException("Unable to extract serial number from 'null' byte array!");
        }
        if (rawBytes.length != LENGTH) {
            throw new ProtocolException("Invalid logical device name [" + ProtocolTools.getHexStringFromBytes(rawBytes) + "]. " +
                    "Expected length [" + LENGTH + "] but was [" + rawBytes.length + "]!");
        }

        final String manufacturerIdentifier = new String(rawBytes, 0, 3);
        final int numberOfPhases;
        switch (rawBytes[6]) {
            case 'M' : {
                numberOfPhases = 1;
                break;
            }
            case 'T' : {
                numberOfPhases = 3;
                break;
            }
            default: {
                numberOfPhases = 0;
                break;
            }
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 10; i < rawBytes.length; i++) {
            final byte rawByte = rawBytes[i];
            sb.append((rawByte >> 4) & 0x0F);
            sb.append(rawByte & 0x0F);
        }

        final String euridisADS = sb.toString().trim();

        return new SerialNumber(manufacturerIdentifier, numberOfPhases, euridisADS);
    }

}
