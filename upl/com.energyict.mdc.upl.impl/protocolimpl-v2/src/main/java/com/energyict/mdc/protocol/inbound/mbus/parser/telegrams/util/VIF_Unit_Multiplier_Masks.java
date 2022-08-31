package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum VIF_Unit_Multiplier_Masks {
    ENERGY_WH(0x07),	// E000 0xxx
    ENERGY_J(0x0F),		// E000 1xxx
    VOLUME(0x17),		// E001 0xxx
    MASS(0x1F),			// E001 1xxx
    ON_TIME(0x23),		// E010 00xx
    OPERATING_TIME(0x27),	// E010 01xx
    POWER_W(0x2F),		// E010 1xxx
    POWER_J_H(0x37),	// E011 0xxx
    VOLUME_FLOW(0x3F),	// E011 1xxx
    VOLUME_FLOW_EXT(0x47),	// E100 0xxx
    VOLUME_FLOW_EXT_S(0x4F),	// E100 1xxx
    MASS_FLOW(0x57),	// E101 0xxx
    FLOW_TEMPERATURE(0x5B),	// E101 10xx
    RETURN_TEMPERATURE(0x5F),	// E101 11xx
    TEMPERATURE_DIFFERENCE(0x63),	// E110 00xx
    EXTERNAL_TEMPERATURE(0x67),	// E110 01xx
    PRESSURE(0x6B),		// E110 10xx
    DATE(0x6C),			// E110 1100
    DATE_TIME_GENERAL(0x6D),	// E110 1101
    DATE_TIME(0x6D),	// E110 1101
    EXTENTED_TIME(0x6D),	// E110 1101
    EXTENTED_DATE_TIME(0x6D),	// E110 1101
    UNITS_FOR_HCA(0x6E),	// E110 1110
    RES_THIRD_VIFE_TABLE(0x6F),	// E110 1111
    AVG_DURATION(0x73),	// E111 00xx
    ACTUALITY_DURATION(0x77),	// E111 01xx
    FABRICATION_NO(0x78),	// E111 1000
    IDENTIFICATION(0x79),	// E111 1001
    ADDRESS(0x7A),			// E111 1010
    // NOT THE ONES FOR SPECIAL PURPOSES
    FIRST_EXT_VIF_CODES(0xFB),	// 1111 1011
    VIF_FOLLOWING(0x7C),	// E111 1100
    SECOND_EXT_VIF_CODES(0xFD),	// 1111 1101
    THIRD_EXT_VIF_CODES_RES(0xEF),	// 1110 1111
    ANY_VIF(0x7E),		// E111 1110
    MANUFACTURER_SPEC(0x7F);	// E111 1111

    private int value;

    private VIF_Unit_Multiplier_Masks(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
