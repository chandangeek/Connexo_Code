/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.common.core;

/**
 * @author sva
 * @since 7/03/2017 - 13:46
 */
public enum DataType {

    A_STRING('A'),
    B_BOOLEAN('B'),
    C_BYTE('C'),
    D_DOUBLE('D'),
    E_EFA('E'),
    F_FLOAT('F'),
    G_STRING_OR_LONG('G'),
    H_HEX_SHORT('H'),
    I_SHORT('I'),
    J_VARIABLE_SPECIAL('J'),
    L_LONG('L'),
    N_INVALID('N'),
    O_FLOAT_ENERGY('O'),
    P_POWER_FACTOR('P'),
    Q_TIME_SECONDS_SINCE_MIDNIGHT('Q'),
    R_DATE_SECONDS_SINCE_1_1_96('R'),
    S_SPECIAL('S'),
    T_TIME_DATE_SINCE__1_97('T'),
    U_DOUBLE_ENERGY('U'),
    V_LONG_LONG('V'),
    W_WAVEFORM('W'),
    X_HEX_LONG('X'),
    Z_HEX_LONG_REGISTER_NR('Z'),
    OTHER('-');

    private final char type;

    DataType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }
}