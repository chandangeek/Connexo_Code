package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.types.UmiCode;

public class ReadObjCmdPayload extends LittleEndianData {
    public static final int SIZE = 4;

    /**
     * The UMI Code of the object
     */
    private final UmiCode umiCode;

    public ReadObjCmdPayload(UmiCode umiCode) {
        this(umiCode, SIZE);
        setRaw(this.umiCode.getRaw());
    }

    public ReadObjCmdPayload(IData data) {
        this(data.getRaw());
    }

    public ReadObjCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        umiCode = new UmiCode(rawPayload);
    }

    protected ReadObjCmdPayload(UmiCode umiCode, int payloadSize) {
        super(payloadSize);
        this.umiCode = umiCode;
        getRawBuffer().put(this.umiCode.getRaw());
    }

    protected ReadObjCmdPayload(byte[] rawPayload, int payloadSize, boolean varLenPayload) {
        super(rawPayload, payloadSize, varLenPayload);
        umiCode = new UmiCode(new byte[] {
                rawPayload[0],
                rawPayload[1],
                rawPayload[2],
                rawPayload[3]});
    }

    public UmiCode getUmiCode() {
        return umiCode;
    }
}
