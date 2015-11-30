package com.energyict.protocolimpl.iec870.ziv5ctd;

/** */

public class FunctionCode {

    int nr;
    FrameType frameType;
    String description;

    public static final int MASK = 0x0F;

    private static FunctionCode create(int nr, FrameType frameType, String description) {
        return new FunctionCode(nr, frameType, description);
    }

    private FunctionCode(int nr, FrameType frameType, String description) {
        this.nr = nr;
        this.frameType = frameType;
        this.description = description;
    }

    static final FunctionCode [] PRIMARY = new FunctionCode[16];
    static final FunctionCode [] SECONDARY = new FunctionCode[16];

    static {
        PRIMARY[0] =
            create(0, FrameType.SEND_CONFIRM, "Reset of remote link");

        PRIMARY[1] =
            create(1, FrameType.SEND_CONFIRM, "Reset of user process");

        PRIMARY[2] =
            create(2, FrameType.SEND_CONFIRM, "Test function for link");

        PRIMARY[3] =
            create(3, FrameType.SEND_CONFIRM, "User data");

        PRIMARY[4] =
            create(4, FrameType.SEND_NO_REPLY, "User data");

        PRIMARY[5] =
            create(5, null, "Reserved");

        PRIMARY[6] =
            create(6, null, "Reserved for special use by agreement");

        PRIMARY[7] =
            create(7, null, "Reserved for special use by agreement");

        PRIMARY[8] =
            create(8, null, "Reserved for unbalanced transmission procedure");

        PRIMARY[9] =
            create(9, FrameType.REQUEST_RESPOND, "Request status of link");

        PRIMARY[10] =
            create(10, null, "Reserved for unbalanced transmission procedure");

        PRIMARY[11] =
            create(11, null, "Reserved for unbalanced transmission procedure");

        PRIMARY[12] =
            create(12, null, "Reserved");

        PRIMARY[13] =
            create(13, null, "Reserved");

        PRIMARY[14] =
            create(14, null, "Reserved for special use by agreement");

        PRIMARY[15] =
            create(15, null, "Reserved for special use by agreement");

        SECONDARY[0] =
            create(0, FrameType.CONFIRM, "ACK: positive acknowledgement");

        SECONDARY[1] =
            create(1, FrameType.CONFIRM, "NACK: message not accepted, link busy");

        SECONDARY[2] =
            create(2, null, "Reserved");

        SECONDARY[3] =
            create(3, null, "Reserved");

        SECONDARY[4] =
            create(4, null, "Reserved");

        SECONDARY[5] =
            create(5, null, "Reserved");

        SECONDARY[6] =
            create(6, null, "Reserved for special use by agreement");

        SECONDARY[7] =
            create(7, null, "Reserved for special use by agreement");

        SECONDARY[8] =
            create(8, FrameType.RESPOND, "User data");

        SECONDARY[9] =
            create(9, FrameType.RESPOND, "NACK: requested data not available");

        SECONDARY[10] =
            create(10, null, "Reserved");

        SECONDARY[11] =
            create(11, FrameType.RESPOND, "Status of link or access demand");

        SECONDARY[12] =
            create(12, null, "Reserved");

        SECONDARY[13] =
            create(13, null, "Reserved for special use by agreement");

        SECONDARY[14] =
            create(14, null, "Link service not functioning");

        SECONDARY[15] =
            create(15, null, "Link service not implemented");
    }

    byte toByte(){
        return (byte)nr;
    }

    byte [] toByteArray(){
        return new byte[] { (byte)nr };
    }

    public String toString() {
        return "FunctionCode [ nr=" + nr + ", " + frameType + ", " + "description=" + description + " ]";
    }

}
