/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame;

import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class GPRSFrame extends AbstractCTRFrame<GPRSFrame> {

    public static final int STX = 0x0A;
    public static final int ETX = 0x0D;

    private int stx;
    private int etx;

    public static final int LENGTH_SHORT = 142;
    public static final int LENGTH_LONG = 1026;

    public GPRSFrame() {
        this(false);
    }

    public GPRSFrame(boolean longFrame) {
        super(longFrame);
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

    /**
     * Parse a given byte array and create a GPRSFrame
     * @param rawPacket: a given byte array
     * @param offset: position to start in the byte array
     * @return the created GPRSFrame
     * @throws CTRParsingException
     */
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
        return super.getLength() + 2;    //+2 = the length of the STX and the ETX field
    }
}
