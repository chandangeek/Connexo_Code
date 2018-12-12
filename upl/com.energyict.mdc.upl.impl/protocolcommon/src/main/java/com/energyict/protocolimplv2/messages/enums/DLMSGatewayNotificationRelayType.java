package com.energyict.protocolimplv2.messages.enums;

/**
 * https://confluence.eict.vpdc/pages/viewpage.action?spaceKey=G3IntBeacon3100&title=DLMS+gateway+specification
 *
 */
public enum DLMSGatewayNotificationRelayType {
    DROP(0, "DROP"),
    PASSTHROUGH(1, "PASSTHROUGH"),
    ADD_ORIGIN_HEADER(2, "ADD_ORIGIN_HEADER"),
    WRAP_AS_SERVER_EVENT(3, "WRAP_AS_SERVER_EVENT");

    private final int attributeNumber;
    private final String authName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param authName description of the attribute
     */
    private DLMSGatewayNotificationRelayType(int attributeNumber, String authName) {
        this.attributeNumber = attributeNumber;
        this.authName = authName;
    }

    public static int fromOptionName(String description) {
        for (DLMSGatewayNotificationRelayType roles : values()) {
            if (roles.getAuthName().equals(description)) {
                return roles.getAttributeNumber();
            }
        }
        return -1;
    }

    public static String[] getOptionNames() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getAuthName();
        }
        return result;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public String getAuthName() {
        return this.authName;
    }

}
