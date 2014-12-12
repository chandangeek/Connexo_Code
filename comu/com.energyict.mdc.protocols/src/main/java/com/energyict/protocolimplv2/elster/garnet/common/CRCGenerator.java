package com.energyict.protocolimplv2.elster.garnet.common;

/**
 * Utility class that can be used to generate the CRC16
 *
 * @author sva
 * @since 22/05/2014 - 9:03
 */
public class CRCGenerator {

    private static final int CRC16_POLYNOMIAL = 0xA001;     // defines the polynomial used to the calculation
    private static final int CRC16_INIT_VECTOR = 0xC181;    // Initial CRC should be set to 0xC181

    /**
     * Generates the CRC16 for the given byte array
     *
     * @param data the byte array for which the CRC16 should be generated
     * @return the generated 16-bits CRC, expressed as integer
     */
    public static int calcCRC16(byte[] data) {
        return calcCRC16(data, data.length);
    }

    /**
     * Generates the CRC16 for the given byte array
     *
     * @param data   the byte array for which the CRC16 should be generated
     * @param length the nr of bytes which should be taken into account (e.g. data.length -2 in case last 2 bytes of data are already reserved for CRC)
     * @return the generated 16-bits CRC, expressed as integer
     */
    public static int calcCRC16(byte[] data, int length) {
        int crc = 0;

        int dataByte = CRC16_INIT_VECTOR;
        for (int i = 0; i < length; i++) {
            dataByte ^= (int) data[i] & 0xFF;
            int j;
            int bitCount;
            for (bitCount = 0; bitCount < 8; dataByte >>= 1, bitCount++) {
                j = (dataByte ^ crc) & 1;
                crc >>= 1;
                if (j == 1) {
                    crc ^= CRC16_POLYNOMIAL;
                }
            }
        }
        return crc;
    }

    // Utility class, hide constructor
    private CRCGenerator() {
    }
}
