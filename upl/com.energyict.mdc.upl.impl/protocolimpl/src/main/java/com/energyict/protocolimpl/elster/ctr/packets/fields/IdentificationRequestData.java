package com.energyict.protocolimpl.elster.ctr.packets.fields;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 9:56:02
 */
public class IdentificationRequestData extends Data {

    public static final int ST_NOT_DEFINED = 0;
    public static final int ST_SAC = 1;
    public static final int ST_TERMINAL = 2;

    private static final int PUCS_LENGTH = 16;

    private final byte[] pucS;
    private final int st;
    private final int stCode;

    public IdentificationRequestData() {
        this.pucS = generateNewPUC_S();
        this.st = ST_NOT_DEFINED;
        this.stCode = 0x0000;
        fillData();
    }

    public IdentificationRequestData(byte[] rawPacket, int offset) {
        int ptr = offset;

        this.pucS = new byte[PUCS_LENGTH];
        for (int i = 0; i < pucS.length; i++) {
            pucS[i] = rawPacket[ptr++];
        }

        st = rawPacket[ptr++] & 0x0FF;

        int tempStCode = (rawPacket[ptr++] << 8) & 0x0FF00;
        stCode = tempStCode + (rawPacket[ptr++] & 0x0FF);

        fillData();
    }

    private void fillData() {
        ByteArrayOutputStream identData = new ByteArrayOutputStream();
        try {
            identData.write(getPucS());
            identData.write(getStAsBytes());
            identData.write(getStCodeAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setData(identData.toByteArray());
    }

    private byte[] getStAsBytes() {
        return new byte[] {(byte) (st & 0x0FF)};
    }

    public int getStCode() {
        return stCode;
    }

    public byte[] getStCodeAsBytes() {
        byte[] stCode = new byte[2];
        stCode[0] = (byte) ((getStCode() >> 8) & 0x0FF);
        stCode[1] = (byte) (getStCode() & 0x0FF);
        return stCode;
    }

    private byte[] generateNewPUC_S() {
        byte[] randomBytes = new byte[16];
        Random random = new Random();

        random.nextBytes(randomBytes);
        return randomBytes;
    }

    public byte[] getPucS() {
        return pucS;
    }

}
