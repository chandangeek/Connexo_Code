package com.energyict.genericprotocolimpl.elster.ctr.frame;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:35:22
 */
public class GPRSFrame extends AbstractCTRFrame<GPRSFrame> {

    private int stx;
    private int etx;

    public byte[] getBytes() {
        return new byte[0];
    }

    public GPRSFrame parse(byte[] rawPacket, int offset) {
        int ptr = offset;

        stx = getIntFromBytes(rawPacket, ptr++, 1);

        super.parse(rawPacket, offset);
        ptr += super.LENGTH;

        etx = getIntFromBytes(rawPacket, ptr++, 1);

        return this;
    }
}
