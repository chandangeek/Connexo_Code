package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.types.UmiCode;

public class GetObjAccessRspPayload extends ReadObjCmdPayload {
    public static final int SIZE = 8;

    /**
     * A 16-bit bitfield giving the roles allowed to read the object value.
     */
    private final int readAccess; // 2 bytes

    /**
     * A 16-bit bitfield giving the roles allowed to write the object value.
     */
    private final int writeAccess; // 2 bytes

    public GetObjAccessRspPayload(UmiCode umiCode, int readAccess, int writeAccess) {
        super(umiCode, SIZE);
        this.readAccess = readAccess;
        this.writeAccess = writeAccess;
        getRawBuffer().putShort((short) readAccess);
        getRawBuffer().putShort((short) writeAccess);
    }

    public GetObjAccessRspPayload(IData data) {
        this(data.getRaw());
    }

    public GetObjAccessRspPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        getRawBuffer().position(UmiCode.SIZE);
        readAccess = getRawBuffer().getShort();
        writeAccess = getRawBuffer().getShort();
    }

    public int getReadAccess() {
        return readAccess;
    }

    public int getWriteAccess() {
        return writeAccess;
    }
}
