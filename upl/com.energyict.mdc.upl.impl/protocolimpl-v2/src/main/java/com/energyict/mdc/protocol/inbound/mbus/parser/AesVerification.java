package com.energyict.mdc.protocol.inbound.mbus.parser;

import org.bouncycastle.util.Strings;

public class AesVerification implements PacketParser {
    private byte byte1;
    private byte byte2;


    @Override
    public int parse(byte[] buffer, int startIndex) {
        int c = startIndex;
        byte1 = buffer[c++];
        byte2 = buffer[c++];

        return c;
    }

    public byte getByte1() {
        return byte1;
    }

    public byte getByte2() {
        return byte2;
    }

    @Override
    public String toString() {
        return String.format("%02X%02X", byte1, byte2);
    }
}
