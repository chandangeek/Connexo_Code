package com.energyict.protocolimpl.modbus.core.functioncode;

/**
 * @author sva
 * @since 20/11/13 - 15:44
 */
public enum FunctionCode {

    READ_COIL_STATUS(1, "Read coil status"),
    READ_INPUT_STATUS(2, "read input status"),
    READ_HOLDING_REGISTER(3, "Read holding register"),
    READ_INPUT_REGISTER(4, "Read input register"),
    WRITE_SINGLE_COIL(5, "Write single coil"),
    WRITE_SINGLE_REGISTER(6, "Write single register"),
    WRITE_MULTIPLE_COILS(15, "Write multiple coils"),
    WRITE_MULTIPLE_REGISTER(16, "Write multiple registers"),
    REPORT_SLAVE_ID(17, "Report slave ID"),
    READ_DEVICE_ID(43, "Read device ID"),
    READ_FILE_RECORD(20, "Read file record");

    private final int functionCode;
    private final String description;

    private FunctionCode(int functionCode, String description) {
        this.functionCode = functionCode;
        this.description = description;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the corresponding FunctionCode
     *
     * @param functionCode The function code.
     * @return The corresponding {@link FunctionCode}.
     */
    public static FunctionCode byFunctionCode(final int functionCode) {
        for (final FunctionCode code : values()) {
            if (code.getFunctionCode() == functionCode) {
                return code;
            }
        }
        return null;
    }
}

