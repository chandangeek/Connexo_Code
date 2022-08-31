package com.energyict.mdc.protocol.inbound.mbus.parser;

public interface PacketParser {

    int parse(byte[] buffer, int startIndex);

    String toString();
}
