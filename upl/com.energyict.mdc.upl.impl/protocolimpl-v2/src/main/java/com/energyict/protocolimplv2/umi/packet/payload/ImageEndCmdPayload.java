package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

public class ImageEndCmdPayload extends LittleEndianData {
    public static final int SIZE = 1;
    private static final byte VALID = 1;
    private static final byte INVALID = 0;

    /**
     * false = Invalid. Abort Image.
     * true = Valid.
     * Sometimes the Source cannot know whether an Image is suitable for use until the end.
     * The Valid field enables the Image Source to tell the Image Target whether the Image is suitable for use. e.g
     * The Source might validate the Image Signature of a Product Image while sending an embedded Device Image to
     * the relevant Device.
     */
    private final boolean valid;

    public ImageEndCmdPayload(boolean valid) {
        super(SIZE);
        this.valid = valid;
        getRawBuffer().put((valid ? VALID : INVALID));
    }

    public ImageEndCmdPayload(byte[] rawPayload) {
        super(rawPayload, SIZE, false);
        valid = getRawBuffer().get() == VALID;
    }

    public boolean getValid() {
        return valid;
    }
}
