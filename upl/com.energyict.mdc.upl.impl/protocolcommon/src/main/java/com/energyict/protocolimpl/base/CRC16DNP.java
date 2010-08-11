package com.energyict.protocolimpl.base;

import com.energyict.protocolimpl.utils.ProtocolTools;

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

    public static void main(String[] args) {
        byte[] bytes = ProtocolTools.getBytesFromHexString("$00$00$00$00$D1$2F$F0$4B$92$97$5F$FE$6F$B8$1C$D0$E9$39$EC$51$F4$0D$9E$E0$EA$8F$45$10$F8$D0$15$82$94$01$87$64$C8$9D$ED$CF$56$36$27$7F$3B$88$24$7E$A5$B7$94$C5$69$E7$27$99$95$A2$AA$0A$49$EF$55$7B$A7$97$19$7C$EA$ED$84$D6$F4$F1$06$86$E5$5D$19$09$A0$42$A6$94$C1$7F$B1$E5$B6$73$29$EA$B1$F1$C0$D6$9E$A6$58$B5$E3$12$40$D4$FC$97$87$81$9D$41$FF$20$6A$38$C5$0A$77$0D$A6$B0$24$66$14$5D$73$F9$30$C7$88$A0$C4$60$D9$7E$7F$80$81$82");
        System.out.println(calcCRC(bytes) + " " + ProtocolTools.getHexStringFromBytes(calcCRCAsBytes(bytes)));

    }
}