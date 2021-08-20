package com.energyict.protocolimplv2.umi.link;

import java.util.Arrays;

public enum LinkLayerError {
    UNKNOWN_FRAME(1, "The frame type was not recognised."),
    UNKNOWN_PROTOCOL_VERSION(2, "The link layer protocol version is not recognised or not supported."),
    BAD_ADDRESS(3, "A peripheral received a frame with an incorrect address."),
    CANNOT_FORWARD_TEMPORARY(4, "The UMI Host was unable to forward the frame to the " +
            "destination specified in the destination address field, but the failure is temporary."),
    BUSY(5, "Device is busy with a UMI Transaction that has not yet completed. This was indicated by Busy = 1 " +
            "in an earlier response frame. Try again later (when the device should be free)."),
    RESERVED(-1, "Unexpected error.")
    ;

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    private String description;

    LinkLayerError(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static LinkLayerError fromId(int id) {
        return Arrays.stream(LinkLayerError.values())
                .filter(error -> error.getValue().equals(id))
                .findFirst()
                .orElse(LinkLayerError.RESERVED);
    }

}
