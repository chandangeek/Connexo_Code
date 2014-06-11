package com.energyict.mdc.protocol.api;

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
public enum ProtocolFamily {

    TEST(1),
    EDMI(2),
    IDIS_GATEWAY(3),
    IDIS_P1(4),
    ELSTER_MULTI_FREQ(5),
    ELSTER_PLC(6),
    ELSTER_IEC(7),
    ELSTER_AM100(8),
    ELSTER_SSWG_EC(9),
    ELSTER_SSWG_IC(10),
    EICT_RTU_EMS(11),
    G3_LINKY_DLMS(12),
    PRIME(13),
    ACTARIS(14),
    CORONIS(15),
    EICT_Z3(16),
    EICT_NTA(17),
    ISKRA_NTA(18),
    ISKRA_PRE_NTA(19),
    DSMR(20),
    DSMR_NTA(21),
    XEMEX(22);

    private int code;

    ProtocolFamily (int code) {
        this.code = code;
    }

    /**
     * Returns the ProtocolFamily that is uniquely identified by the code.
     *
     * @param code The code
     * @return The ProtocolFamily or <code>null</code> if no ProtocolFamily is uniquely identified by the code
     */
    public static ProtocolFamily fromCode (int code) {
        for (ProtocolFamily rule : values()) {
            if (code == rule.code) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Gets the code that should be used in the license file
     * to indicate that the protocol family is covered by the license.
     *
     * @return The code
     */
    public int getCode () {
        return this.code;
    }

    /**
     * Gets the name of this ProtocolFamily, note that this is NOT localized yet.
     *
     * @return The name of this ProtocolFamily
     */
    public String getName () {
        return this.name();
    }

}