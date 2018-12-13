package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.obis.ObisCode;

/**
 * Created by sva on 16/03/2017.
 */
public enum MK10InstantaneousRegisterInformation {

    VOLTAGE_PHASE_A(0xE000, ObisCode.fromString("1.1.32.7.0.255")),
    VOLTAGE_PHASE_B(0xE001, ObisCode.fromString("1.1.52.7.0.255")),
    VOLTAGE_PHASE_C(0xE002, ObisCode.fromString("1.1.72.7.0.255")),

    VOLTAGE_AB(0xE004, ObisCode.fromString("1.1.12.7.1.255")),
    VOLTAGE_BC(0xE005, ObisCode.fromString("1.1.12.7.2.255")),
    VOLTAGE_CA(0xE006, ObisCode.fromString("1.1.12.7.3.255")),

    FUNDAMENTAL_VOLTAGE_PHASE_A(0xE007, ObisCode.fromString("1.1.32.7.1.255")),
    FUNDAMENTAL_VOLTAGE_PHASE_B(0xE008, ObisCode.fromString("1.1.52.7.1.255")),
    FUNDAMENTAL_VOLTAGE_PHASE_C(0xE009, ObisCode.fromString("1.1.72.7.1.255")),

    THD_PHASE_A(0xE00A, ObisCode.fromString("1.1.32.7.124.255")),
    THD_PHASE_B(0xE00B, ObisCode.fromString("1.1.52.7.124.255")),
    THD_PHASE_C(0xE00C, ObisCode.fromString("1.1.72.7.124.255")),

    UNBALANCED_VOLTAGE_ZERO_SEQUENCE(0xE00D, ObisCode.fromString("1.1.32.7.2.255")),
    UNBALANCED_VOLTAGE_POSITIVE_SEQUENCE(0xE00E, ObisCode.fromString("1.1.52.7.2.255")),
    UNBALANCED_VOLTAGE_NEGATIVE_SEQUENCE(0xE00F, ObisCode.fromString("1.1.72.7.2.255")),

    CURRENT_PHASE_A(0xE010, ObisCode.fromString("1.1.31.7.0.255")),
    CURRENT_PHASE_B(0xE011, ObisCode.fromString("1.1.51.7.0.255")),
    CURRENT_PHASE_C(0xE012, ObisCode.fromString("1.1.71.7.0.255")),
    CURRENT_NEUTRAL(0xE013, ObisCode.fromString("1.1.91.7.0.255")),

    PHASE_ANGLE_A(0xE020, ObisCode.fromString("1.1.81.7.0.255")),
    PHASE_ANGLE_B(0xE021, ObisCode.fromString("1.1.81.7.11.255")),
    PHASE_ANGLE_C(0xE022, ObisCode.fromString("1.1.81.7.22.255")),

    POWER_FACTOR_PHASE_A(0xE023, ObisCode.fromString("1.1.33.7.0.255")),
    POWER_FACTOR_PHASE_B(0xE024, ObisCode.fromString("1.1.53.7.0.255")),
    POWER_FACTOR_PHASE_C(0xE025, ObisCode.fromString("1.1.73.7.0.255")),
    POWER_FACTOR(0xE026, ObisCode.fromString("1.1.13.7.0.255")),

    PHASE_ANGLE_CURRENT_TO_VOLTAGE_PHASE_A(0xE027, ObisCode.fromString("1.1.81.7.4.255")),
    PHASE_ANGLE_CURRENT_TO_VOLTAGE_PHASE_B(0xE028, ObisCode.fromString("1.1.81.7.14.255")),
    PHASE_ANGLE_CURRENT_TO_VOLTAGE_PHASE_C(0xE029, ObisCode.fromString("1.1.81.7.24.255")),

    PHASE_ANGLE_VOLTAGE_TO_VOLTAGE_PHASE_B_TO_A(0xE02B, ObisCode.fromString("1.1.81.7.10.255")),
    PHASE_ANGLE_VOLTAGE_TO_VOLTAGE_PHASE_C_TO_A(0xE02C, ObisCode.fromString("1.1.81.7.20.255")),

    ACTIVE_POWER_PHASE_A(0xE030, ObisCode.fromString("1.1.21.7.0.255")),
    ACTIVE_POWER_PHASE_B(0xE031, ObisCode.fromString("1.1.41.7.0.255")),
    ACTIVE_POWER_PHASE_C(0xE032, ObisCode.fromString("1.1.61.7.0.255")),
    ACTIVE_POWER(0xE033, ObisCode.fromString("1.1.1.7.0.255")),

    REACTIVE_POWER_PHASE_A(0xE040, ObisCode.fromString("1.1.23.7.0.255")),
    REACTIVE_POWER_PHASE_B(0xE041, ObisCode.fromString("1.1.43.7.0.255")),
    REACTIVE_POWER_PHASE_C(0xE042, ObisCode.fromString("1.1.63.7.0.255")),
    REACTIVE_POWER(0xE043, ObisCode.fromString("1.1.3.7.0.255")),

    APPARENT_POWER_PHASE_A(0xE050, ObisCode.fromString("1.1.29.7.0.255")),
    APPARENT_POWER_PHASE_B(0xE051, ObisCode.fromString("1.1.49.7.0.255")),
    APPARENT_POWER_PHASE_C(0xE052, ObisCode.fromString("1.1.69.7.0.255")),
    APPARENT_POWER(0xE053, ObisCode.fromString("1.1.9.7.0.255")),

    FREQUENCY(0xE060, ObisCode.fromString("1.1.14.7.0.255"))
    ;

    private final int registerId;
    private final ObisCode obisCode;

    MK10InstantaneousRegisterInformation(int registerId, ObisCode obisCode) {
        this.registerId = registerId;
        this.obisCode = obisCode;
    }

    public int getRegisterId() {
        return registerId;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public static MK10InstantaneousRegisterInformation fromObisCode(ObisCode obisCode){
        for (MK10InstantaneousRegisterInformation registerInformation : values()) {
            if (registerInformation.getObisCode().equals(obisCode)) {
                return registerInformation;
            }
        }
        return null;
    }
}