package com.energyict.protocolimplv2.umi.link;

import java.util.Arrays;

public enum FragmentationError {
    PACKAGE_TOO_LARGE(0,"The receiving fragmentation sub-layer cannot accept the incoming application layer packet " +
            "because it is too long."),
    FRAGMENTATION_PROTOCOL_ERROR(1,"An unexpected fragment type was received"),
    LENGTH_ERROR(2,"The length of the received re-assembled data exceeds the expected length (from the First fragment) " +
            "or the Last fragment was received before all the expected data (from the First fragment) was received."),
    TERMINATED(3,"The end of fragmentation"),
    UNKNOWN_FRAGMENTATION_PROTOCOL_VERSION(4,"The fragmentation sub-layer protocol version is not recognised " +
            "or not supported."),
    ERROR(-1, "Unexpected error occurred.")
    ;

    private int id;
    private String description;

    FragmentationError (int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static FragmentationError fromId(int id) {
        return Arrays.stream(FragmentationError.values())
                .filter(val -> val.getId() == id)
                .findFirst()
                .orElse(FragmentationError.ERROR);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
