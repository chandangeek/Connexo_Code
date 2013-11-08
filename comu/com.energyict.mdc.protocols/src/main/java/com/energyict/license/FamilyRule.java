package com.energyict.license;

import com.energyict.mdw.core.ProtocolFamily;

/**
 * Models the known groups of protocols that are united by a significant shared characteristic.
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
 * @since 2013-07-18 (11:49)
 */
public enum FamilyRule implements ProtocolFamily {

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

    FamilyRule (int code) {
        this.code = code;
    }

    /**
     * Gets the code that should be used in the license file
     * to indicate that the FamilyRule is covered by the license.
     *
     * @return The code
     */
    public int getCode () {
        return code;
    }

    /**
     * Returns the FamilyRule that is uniquely identified by the code.
     *
     * @param code The code
     * @return The FamilyRule or <code>null</code> if no FamilyRule is uniquely identified by the code
     */
    public static ProtocolFamily fromCode (int code) {
        for (FamilyRule rule : values()) {
            if (code == rule.code) {
                return rule;
            }
        }
        return null;
    }

    @Override
    public String getName () {
        return this.name();
    }

}