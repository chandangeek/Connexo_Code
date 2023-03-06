/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser;

public class DataLinkLayer implements PacketParser {

    private byte length;        // L-Field = length of data
    private byte sendNoReply;   // C-Field = send - no reply
    private int fabMan;         // M-Field = LSB, MSB
    private int fabId;          // A-Field = 4 bytes: LSB, x, x, MSB
    private int fabVersion;     // A-Field = 11h
    private int fabDevType;     // A-Field = 37h = radio converter meter side


    @Override
    public int parse(byte[] buffer, int startIndex) {
        int c = startIndex;
        length = buffer[c++];
        sendNoReply = buffer[c++];
        fabMan = buffer[c++] + 0x100 * buffer[c++];
        fabId = buffer[c++] + 0x100 * buffer[c++] + 0x10000 * buffer[c++] + 0x1000000 * buffer[c++];
        fabVersion = buffer[c++];
        fabDevType = buffer[c++];
        return c;
    }

    @Override
    public String toString() {
        final String SEP = ", ";
        String sb = "DLL: " +
                "Length of data=" + String.format("%02X", getLength()) + "h" + SEP +
                "Send-No Reply=" + String.format("%02X", getSendNoReply()) + "h" + SEP +
                "FabMan=" + String.format("%04X", getFabMan()) + "h" + SEP +
                "FabId=" + getFabId() + SEP +
                "FabVersion" + String.format("%02X", getFabVersion()) + "h" + SEP +
                "FabDevType" + String.format("%02X", getFabDevType()) + "h";
        return sb;
    }


    public byte getLength() {
        return length;
    }

    public byte getSendNoReply() {
        return sendNoReply;
    }

    public int getFabMan() {
        return fabMan;
    }

    public int getFabId() {
        return fabId;
    }

    public int getFabVersion() {
        return fabVersion;
    }

    public int getFabDevType() {
        return fabDevType;
    }
}
