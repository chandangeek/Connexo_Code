package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.Limits;

public class ImageDataCmdPayload extends LittleEndianData {
    public static int MIN_SIZE = 5;

    /**
     * A sequence number. The first Image Data command has sequence number zero.
     * The sequence number increments by one in successive Image Data commands.
     */
    private final long sequenceNumber; // 4 bytes

    /**
     * Data from the image.
     */
    private final byte[] data;

    public ImageDataCmdPayload(byte[] rawPayload) {
        super(rawPayload, MIN_SIZE, true);
        this.sequenceNumber = getRawBuffer().getInt();
        this.data = new byte[rawPayload.length - MIN_SIZE + 1];
        this.getRawBuffer().get(this.data);
    }

    public ImageDataCmdPayload(long sequenceNumber, byte[] data) {
        super(MIN_SIZE + data.length - 1);
        if (data.length == Limits.MIN_UNSIGNED || data.length > Limits.MAX_UNSIGNED_INT) {
            throw new java.security.InvalidParameterException(
                    "Invalid imageLength=" + data.length + ", range=[" +
                            (Limits.MIN_UNSIGNED + 1) + ", " + Limits.MAX_UNSIGNED_INT + "]"
            );
        }
        this.sequenceNumber = sequenceNumber;
        this.data = data.clone();
        getRawBuffer().putInt((int)sequenceNumber).put(data);
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }
}
