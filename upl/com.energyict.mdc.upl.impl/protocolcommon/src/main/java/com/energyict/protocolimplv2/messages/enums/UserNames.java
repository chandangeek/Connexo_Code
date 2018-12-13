package com.energyict.protocolimplv2.messages.enums;


/**
 * Copyrights EnergyICT
 * Date: 12/07/16
 * Time: 17:59
 *
 */
public enum UserNames{

    ADMIN(1, "admin"),
    READWRITE(2, "readwrite"),
    READONLY(3, "readonly");

    private final int attributeNumber;
    private final String name;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param name       the shortName of the attribute
     */
    private UserNames(int attributeNumber, String name) {
        this.attributeNumber = attributeNumber;
        this.name = name;
    }

    public static int fromName(String description) {
        for (UserNames roles : values()) {
            if (roles.getName().equals(description)) {
                return roles.getAttributeNumber();
            }
        }
        return -1;
    }

    public static String[] getAllNames() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getName();
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
    public String getName() {
        return this.name;
    }

/*    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }*/
}
