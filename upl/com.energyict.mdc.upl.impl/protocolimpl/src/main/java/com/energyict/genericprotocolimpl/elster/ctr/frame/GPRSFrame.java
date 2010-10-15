package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.GprsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:35:22
 */
public class GPRSFrame extends AbstractCTRFrame<GPRSFrame> {

    public static final int STX = 0x0A;
    public static final int ETX = 0x0D;

    private int stx;
    private int etx;

    public static final int LENGTH_SHORT = 142;

    public GPRSFrame() {
        this.stx  = STX;
        this.etx = ETX;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[getLength()];
        bytes[0] = (byte) stx;
        byte[] ctrFrameBytes = super.getBytes();
        System.arraycopy(ctrFrameBytes, 0, bytes, 1, ctrFrameBytes.length);
        bytes[getLength() - 1] = (byte) etx;
        return bytes;
    }

    public GPRSFrame parse(byte[] rawPacket, int offset) throws CTRParsingException {
        int ptr = offset;
        stx = getIntFromBytes(rawPacket, ptr++, 1);
        super.parse(rawPacket, ptr);
        ptr += super.getLength();
        etx = getIntFromBytes(rawPacket, ptr++, 1);
        return this;
    }

    public GPRSFrame sendAndGetResponse(GprsConnection connection) throws CTRConnectionException {
        return connection.sendFrameGetResponse(this);
    }

    @Override
    public int getLength() {
        return super.getLength() + 2;
    }
}
