package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 12/10/16
 * Time: 13:59
 */
public enum LoadProfileOptInOut {

    ActivateCapturingInLoadProfile1And2(1, "Activate the capturing in load profile 1 and 2"),
    DeactivateCapturingInLoadProfile1And2(2, "Deactivate the capturing in load profile 1 and 2"),
    ActivateCapturingInLoadProfile1(3, "Activate the capturing in load profile 1"),
    DeactivateCapturingInLoadProfile1(4, "Deactivate the capturing in load profile 1"),
    ActivateCapturingInLoadProfile2(5, "Activate the capturing in load profile 2"),
    DeactivateCapturingInLoadProfile2(6, "Deactivate the capturing in load profile 2");

    private final int attributeNumber;
    private final String profileName;

    /**
     * Default constructor
     *
     * @param attributeNumber the number of the attribute (1-based)
     * @param profName description of the attribute
     */
    private LoadProfileOptInOut(int attributeNumber, String profName) {
        this.attributeNumber = attributeNumber;
        this.profileName = profName;
    }

    public static int fromScriptName(String description) {
        for (com.energyict.protocolimplv2.messages.enums.LoadProfileOptInOut roles : values()) {
            if (roles.getScriptName().equals(description)) {
                return roles.getAttributeNumber();
            }
        }
        return 0;
    }

    public static String[] getScriptNames() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getScriptName();
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
    public String getScriptName() {
        return this.profileName;
    }

}

