package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 15/07/16
 * Time: 13:59
 */
public enum AuthenticationMechanism {


    NONE(0, "none"),
    PASSWORD(1, "password"),
    CERTIFICATE(2, "certificate"),
    CERTIFICATE_AND_PASSWORD(3, "certificate_and_password");

    private final int attributeNumber;
    private final String authName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param authName description of the attribute
     */
    private AuthenticationMechanism(int attributeNumber, String authName) {
        this.attributeNumber = attributeNumber;
        this.authName = authName;
    }

    public static int fromAuthName(String description) {
        for (AuthenticationMechanism roles : values()) {
            if (roles.getAuthName().equals(description)) {
                return roles.getAttributeNumber();
            }
        }
        return -1;
    }

    public static String[] getAuthNames() {
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
