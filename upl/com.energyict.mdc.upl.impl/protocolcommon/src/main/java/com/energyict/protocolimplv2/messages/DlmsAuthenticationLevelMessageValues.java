package com.energyict.protocolimplv2.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 18/03/13
 * Time: 16:39
 */
public enum DlmsAuthenticationLevelMessageValues {

    NO_AUTHENTICATION("No authentiction", 0),
    LOW_LEVEL("Low level authentication", 1),
    MANUFACTURER_SPECIFIC("Manufacturer specific", 2),
    HIGH_LEVEL_MD5("High level authentication - MD5", 3),
    HIGH_LEVEL_SHA1("High level authentication - SHA-1", 4),
    HIGH_LEVEL_GMAC("High level authentication - GMAC", 5);

    private final String name;
    private final int value;

    private DlmsAuthenticationLevelMessageValues(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public static String[] getNames(){
        List<String> names = new ArrayList<>();
        for (DlmsAuthenticationLevelMessageValues dlmsAuthenticationLevelMessageValues : values()) {
            names.add(dlmsAuthenticationLevelMessageValues.name);
        }
        return names.toArray(new String[names.size()]);
    }

    public static int getValueFor(final String name){
        for (DlmsAuthenticationLevelMessageValues dlmsAuthenticationLevelMessageValues : values()) {
            if(dlmsAuthenticationLevelMessageValues.name.equals(name)){
                return dlmsAuthenticationLevelMessageValues.getValue();
            }
        }
        return -1;
    }

    public String getName() {
        return name;
    }
}
