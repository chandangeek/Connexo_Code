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
        StringBuilder sb = new StringBuilder();
        sb.append("DLL: ");
        sb.append("Length of data=").append(String.format("%02X", getLength())).append("h").append(SEP);
        sb.append("Send-No Reply=").append(String.format("%02X", getSendNoReply())).append("h").append(SEP);
        sb.append("FabMan=").append(String.format("%04X", getFabMan())).append("h").append(SEP);
        sb.append("FabId=").append(getFabId()).append(SEP);
        sb.append("FabVersion").append(String.format("%02X", getFabVersion())).append("h").append(SEP);
        sb.append("FabDevType").append(String.format("%02X", getFabDevType())).append("h");
        return sb.toString();
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
