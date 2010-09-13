package com.energyict.dlms.aso;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all DLMS AuthenticationLevels
 *
 * <p>
 * Copyrights EnergyICT
 * Date: 6-sep-2010
 * Time: 14:03:59
 * </p>
 */
public enum AuthenticationTypes {

    LOWEST_LEVEL(0, "NoAlgorithm"),
    LOW_LEVEL(1, "NoAlgorithm"),
    MAN_SPECIFIC_LEVEL(2, "ManSpecific"),
    HLS3(3, "MD5"),
    HLS4(4, "SHA-1"),
    HLS5(5, "GMAC");

    private int level;
    private String algorithmName;
    private static Map<Integer, AuthenticationTypes> instances;

    /**
     * Private constructor
     * @param level the level of the authentication
     * @param algorithmName the name of the authentication algorithm to be applied
     */
    private AuthenticationTypes(int level, String algorithmName) {
        this.level = level;
        this.algorithmName = algorithmName;
        getInstances().put(this.level, this);
    }

    /**
     * @param level the level of authentication
     * @return the {@link com.energyict.dlms.aso.AuthenticationTypes} according to the level
     */
    public static AuthenticationTypes getTypeFor(int level) {
        return getInstances().get(level);
    }

    /**
     * @return the complete list of authenticationTypes
     */
    private static Map<Integer, AuthenticationTypes> getInstances() {
        if (instances == null) {
            instances = new HashMap<Integer, AuthenticationTypes>(6);
        }
        return instances;
    }

    /**
     * @return the Authentication algorithm name
     */
    public String getAlgorithm(){
        return this.algorithmName;
    }
}
