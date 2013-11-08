package com.energyict.dlms.aso;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all current DLMS AuthenticationLevels.<br>
 * Currently there are 6:<br>
 * <ul>
 * <li>{@link #LOWEST_LEVEL}
 * <li>{@link #LOW_LEVEL}
 * <li>{@link #MAN_SPECIFIC_LEVEL}
 * <li>{@link #HLS3_MD5}
 * <li>{@link #HLS4_SHA1}
 * <li>{@link #HLS5_GMAC}
 * </ul>
 *
 * <p>
 * Copyrights EnergyICT
 * Date: 6-sep-2010
 * Time: 14:03:59
 * </p>
 */
public enum AuthenticationTypes {

    /**
     * No authentication is applied
     */
    LOWEST_LEVEL(0, "NoAlgorithm"),
    /**
     * Authentication is done by sending over the password to the device in plainText
     */
    LOW_LEVEL(1, "NoAlgorithm"),
    /**
     * Manufacturers can create there own specification for the AuthenticationProcess.<br>
     * Currently we do not implement this as we want to be able to talk to multiple meter vendors.
     */
    MAN_SPECIFIC_LEVEL(2, "ManSpecific"),
    /**
     * A 4-step handshaking method is used to authenticate both client and server. The encryption method used is MD5
     */
    HLS3_MD5(3, "MD5"),
    /**
     *A 4-step handshaking method is used to authenticate both client and server. The encryption method used is SHA-1
     */
    HLS4_SHA1(4, "SHA-1"),
    /**
     * A 4-step handshaking method is used to authenticate both client and server. The encryption method used is GMAC
     */
    HLS5_GMAC(5, "GMAC");

    /**
     * The AuthenticationLevel as it is given in the SecurityLevel property
     */
    private int level;
    /**
     * The Algorithm name used for giving to the encryption method
     */
    private String algorithmName;
    /**
     * A map containing all instances of this enumeration
     */
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
