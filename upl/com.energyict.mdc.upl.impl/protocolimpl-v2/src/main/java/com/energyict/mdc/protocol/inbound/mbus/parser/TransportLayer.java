/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser;

public class TransportLayer implements PacketParser{
    private byte header;
    private byte tplAccessNumber;
    private byte meterState;
    private int configField;


    @Override
    public int parse(byte[] buffer, int startIndex) {
        int c = startIndex;
        header = buffer[c++];
        tplAccessNumber = buffer[c++];
        meterState = buffer[c++];
        configField = 0x100 * buffer[c++] + buffer[c++] ;

        return c;
    }

    @Override
    public String toString() {
        final String SEP = ", ";
        return "TransportLayer: " +
                "header=" + String.format("%02X", getHeader()) + "h" + SEP +
                "TPL access number=" + String.format("%02X", getTplAccessNumber()) + "h" + SEP +
                "Meter State=" + String.format("%8s", Integer.toBinaryString(getMeterState() & 0xFF)).replace(' ', '0') + SEP +
                "Config field=" + String.format("%02X", getConfigField()) + "h" + SEP;
    }

    public byte getHeader() {
        return header;
    }

    public byte getTplAccessNumber() {
        return tplAccessNumber;
    }

    public byte getMeterState() {
        return meterState;
    }

    public int getConfigField() {
        return configField;
    }

}
