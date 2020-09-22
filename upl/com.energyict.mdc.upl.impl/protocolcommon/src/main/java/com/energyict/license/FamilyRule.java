package com.energyict.license;

import com.energyict.mdc.protocol.ProtocolFamily;

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

    TEST(1),                // Test
    EDMI(2),                // EDMI MK10 CommandLine
    ELSTER_IDIS(3),         // Elster IDIS DLMS
    ELSTER_IDIS_P1(4),      // ELSTER IDIS DLMS (P1)
    ELSTER_MULTI_FREQ(5),   // Elster SFSK PLC (MF) DLMS
    ELSTER_IDIS_P2(6),      // ELSTER IDIS DLMS (P2)
    ELSTER_IEC(7),          // Elster AS220/AS1440 IEC1107
    ELSTER_AM100(8),        // Elster AM100 (PRE-NTA) DLMS
    ELSTER_SSWG_EC(9),      // Elster SSWG (EC) DLMS
    ELSTER_SSWG_IC(10),     // Elster SSWG (IC) DLMS
    EICT_RTU_EMS(11),       // EnergyICT RTU EMS
    G3_LINKY_DLMS(12),      // G3 (Linky) DLMS
    PRIME(13),              // PRIME AMM T5(PRIME1.5) DLMS
    ACTARIS(14),            // Actaris ACE4000 MarkIII MeterXML
    CORONIS(15),            // Coronis Wavenis
    EICT_Z3(16),            // EnergyICT WebRTU Z3 DLMS
    EICT_NTA(17),           // NTA DSMR2.3 DLMS
    ISKRA_PRE_NTA(19),      // Iskraemeco PRE-NTA DLMS
    DSMR_NTA(21),           // NTA DSMR4.0 DLMS
    XEMEX(22),              // Xemex ReMI DLMS
    EDP_DLMS(23),           // EDP Public Lights Monitoring DLMS
    PPM(24),                // ABB/GE PPM IEC1107
    CEWE_PROMETER(25),      // Cewe Prometer IEC1107
    ELSTER_DL_LIS200(26),   // Elster DLxxx LIS200
    ELSTER_Ek_LIS200(27),   // Elster EKxxx LIS200
    ENERDIS_ENERIUM(28),    // Enerdis Enerium Modbus
    ENERDIS_RECDIGIT(29),   // Enerdis Recdigit Modbus
    KV_ANSI(30),            // General Electric KVx ANSI
    POREG(31),              // Iskraemeco Poreg 2x DIN19244
    LG_SCTM(32),            // L&G Dataloggers SCTM
    ELSTER_GARNET(33),      // Elster GARNET
    G3_PLC(34),             // G3 PLC
    EICT_SUBMETER_EMS(35),  // EnergyICT Submeter EMS
    EICT_THEMISUNO(36),     // Elster THEMISUNO
    ACUD(37);               // ACUD Devices
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