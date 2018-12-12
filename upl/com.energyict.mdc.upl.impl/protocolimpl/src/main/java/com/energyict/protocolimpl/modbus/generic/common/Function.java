package com.energyict.protocolimpl.modbus.generic.common;

/**
 * @author sva
 * @since 18/11/13 - 14:39
 */
public enum Function {
    
    READ_COIL_STATUS(1),
    READ_INPUT_STATUS(2),
    READ_HOLDING_REGISTERS(3),
    READ_INPUT_REGISTERS(4),
    PRESET_SINGLE_REGISTER(6),
    PRESET_MULTIPLE_REGISTERS(16),
    REPORT_SLAVE_ID(17),
    UNKNOWN(-1);

    private final int functionCode;

    private Function(int functionCode) {
        this.functionCode = functionCode;
    }

    public static Function getFunction(int functionCode) {
        for (Function function : Function.values()) {
            if (function.functionCode == functionCode) {
                return function;
            }
        }
        return Function.UNKNOWN;
    }
}
