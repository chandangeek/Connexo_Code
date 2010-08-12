package com.energyict.protocolimpl.elster.ctr.packets.fields;

import com.energyict.protocolimpl.base.CRC16DNP;
import com.energyict.protocolimpl.elster.ctr.packets.CTRPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:46:42
 */
public class Crc extends AbstractPacketField {

    public static final int LENGTH = 2;

    private final byte[] crcValue;

    public Crc(CTRPacket dataField) {
        ByteArrayOutputStream crcData = new ByteArrayOutputStream();
        try {
            crcData.write(dataField.getAddress().getBytes());
            crcData.write(dataField.getClientProfile().getBytes());
            crcData.write(dataField.getFunctionCode().getBytes());
            crcData.write(dataField.getAleo().getBytes());
            crcData.write(dataField.getStructureCode().getBytes());
            crcData.write(dataField.getChannel().getBytes());
            crcData.write(dataField.getCpa().getBytes());
            crcData.write(dataField.getData().getBytes());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        crcValue = CRC16DNP.calcCRCAsBytes(crcData.toByteArray());
    }

    public Crc(byte[] rawPacket, int offset) {
        crcValue = new byte[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            crcValue[i] = rawPacket[offset + i];
        }
    }

    public byte[] getBytes() {
        return crcValue;
    }

}
