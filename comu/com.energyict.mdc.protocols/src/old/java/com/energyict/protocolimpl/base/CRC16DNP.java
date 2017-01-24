package com.energyict.protocolimpl.base;

public final class CRC16DNP {

    private static final int[] crcTable = new int[256];
    private static final int P_DNP = 0xA6BC;

    static {
        for (int i = 0; i < 256; i++) {
            int crc = 0, c = i;
            for (int j = 0; j < 8; j++) {
                if (((crc ^ c) & 0x01) != 0) {
                    crc = (crc >> 1) ^ P_DNP;
                } else {
                    crc >>= 1;
                }
                c >>= 1;
            }
            crcTable[i] = crc;
        }
    }

    /**
     * Calc 16-bit CRC using the current polynomial
     *
     * @param ba byte array to compute CRC on
     * @return 16-bit CRC, unsigned
     */
    public static int calcCRC(byte[] ba) {
        int crc = 0;
        for (byte b : ba) {
            int tableItem = crcTable[(crc ^ (0x00ff & b)) & 0x0ff];
            crc = (crc >> 8) ^ tableItem;
        }
        crc = ((crc << 8) & 0x0FF00) + ((crc >> 8) & 0x0FF);
        crc = ~crc & 0x0FFFF;
        return crc;
    }

    /**
     * Calc 16-bit CRC using the current polynomial
     *
     * @param ba byte array to compute CRC on
     * @return 16-bit CRC, unsigned
     */
    public static byte[] calcCRCAsBytes(byte[] ba) {
        int crc = calcCRC(ba);
        byte[] crcBytes = new byte[2];
        crcBytes[0] = (byte) (crc >> 8 & 0x0FF);
        crcBytes[1] = (byte) (crc & 0x0FF);
        return crcBytes;
    }

}