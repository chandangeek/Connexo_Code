package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.Limits;

public class ImageStartCmdPayload extends LittleEndianData {
    public static final int SIZE = 4;

    /**
     * The length of the entire image, in bytes
     * size = 4 bytes
     */
    private final long imageLength;

    public ImageStartCmdPayload(long imageLength) {
        super(SIZE);
        if (imageLength <= Limits.MIN_UNSIGNED || imageLength > Limits.MAX_UNSIGNED_INT) {
            throw new java.security.InvalidParameterException(
                    "Invalid imageLength=" + imageLength + ", range=[" +
                            (Limits.MIN_UNSIGNED + 1) + ", " + Limits.MAX_UNSIGNED_INT + "]"
            );
        }
        this.imageLength = imageLength;
        getRawBuffer().putInt((int)imageLength);
    }

    public ImageStartCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        imageLength = Integer.toUnsignedLong(getRawBuffer().getInt());
    }

    public long getImageLength() {
        return imageLength;
    }
}
