package com.energyict.mdc.protocol;

/**
 * Represents a groups of protocols that are united by a significant shared characteristic.
 * This implies that a protocol can be part of multiple families depending on its
 * characteristics and how significantly shared this characteristic is with other protocols.
 * Examples of such characteristics are:
 * <ul>
 * <li>Manufacturer</li>
 * <li>Metering functionality</li>
 * <li>Protocol capability</li>
 * </ul>
 * <p>
 * The idea is that an entire family of protocols can be covered by a single license file entry.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (15:11)
 */
public interface ProtocolFamily {

    /**
     * Gets the code that should be used in the license file
     * to indicate that the protocol family is covered by the license.
     *
     * @return The code
     */
    int getCode();

    /**
     * Gets the name of this ProtocolFamily, note that this is NOT localized yet.
     *
     * @return The name of this ProtocolFamily
     */
    String getName();

}