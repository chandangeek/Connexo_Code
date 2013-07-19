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
    DLMS(2),
    MODBUS(3),
    TRIMARAN(4),
    ELSTER(5),
    LIS(6),
    DSFG(7),
    ITRON(8),
    LANDISGYR(9),
    VDEW(10),
    ENERGYICT(11),
    DSMR(12),
    FERRANTI(13),
    KENDA(14),
    ISKRA(15),
    EDMI(16),
    SIEMENS(17),
    EMON(18),
    AMETEK(19),
    CORONIS(20),
    IDIS(21),
    IEC(22),
    EDF(23),
    GE(24),
    SCHNEIDER(25),
    SOCOMEC(26),
    ENERDIS(27),
    SQUARED(28),
    VERIS(29),
    FLONIDAN(30),
    MBUS(31),
    POREG(32),
    ENERMET(33),
    GMC(34),
    EMCO(35),
    PRI(36),
    ACTARIS(37),
    POWER_MEASUREMENT(38),
    PRIME(39),
    METCOM(40),
    SCTM(41),
    NORTHERN_DESIGN(42),
    KAMSTRUP(43),
    SAMPLE(44),
    NTA(45),
    IBM(46),
    G3(47),
    ECHELON(48);

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