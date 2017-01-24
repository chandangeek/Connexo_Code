package com.energyict.protocolimpl.din19244.poreg2.core;

/**
 * Enum containing all possible response ID's, including a description.
 *
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 17:35:30
 */
public enum Response {

    ACK(0, "ACK"),
    NACK1(1, "NACK: message is not accepted"),
    USERDATA(8, "user data"),
    NACK2(9, "NACK: data not available"),
    STATUS(11, "status of link");

    private int id;
    private String description;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    Response(int id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Get the description for a certain response
     */
    public static String getDescription(int ack) {
        for (Response response : values()) {
            if (response.getId() == ack) {
                return response.getDescription();
            }
        }
        return "unknown response";
    }
}
