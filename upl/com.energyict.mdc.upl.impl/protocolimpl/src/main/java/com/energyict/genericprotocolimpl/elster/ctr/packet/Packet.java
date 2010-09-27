package com.energyict.genericprotocolimpl.elster.ctr.packet;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:32:29
 */
public interface Packet {

    byte[] getBytes();

    void parse(byte[] rawPacket, int offset);

}
