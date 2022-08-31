package com.energyict.mdc.protocol.inbound.mbus.parser;

public class TransportLayer implements PacketParser{
    private byte header;
    private byte tplAccessNumber;
    private byte meterState; // TODO: enum here
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
        StringBuilder sb = new StringBuilder();
        sb.append("TransportLayer: ");
        sb.append("header=").append(String.format("%02X", getHeader())).append("h").append(SEP);
        sb.append("TPL access number=").append(String.format("%02X", getTplAccessNumber())).append("h").append(SEP);
        sb.append("Meter State=").append(String.format("%8s",  Integer.toBinaryString(getMeterState() & 0xFF)).replace(' ', '0')).append("").append(SEP);
        sb.append("Config field=").append(String.format("%02X", getConfigField())).append("h").append(SEP);
        return sb.toString();
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
