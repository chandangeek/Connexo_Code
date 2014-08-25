package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class Function extends AbstractField<Function> {

    public static enum FunctionCode {

        ACK(0x06),
        NACK(0x15),
        WAIT(0x10),
        ACTUAL_PARAMETERS_WITH_DEMAND_RESET(0x20),
        ACTUAL_PARAMETERS(0x21),
        PREVIOUS_PARAMETERS(0x22),
        ACTUAL_PARAMETERS_WITH_FULL_LP(0x51),
        CURRENT_REGISTERS(0x23),
        PREVIOUS_REGISTERS(0x24),
        POWER_FAIL_LOG(0x25),
        HISTORY_LOG(0x28),
        INSTRUMENTATION_PAGE(0x14),
        LP_OF_CURRENT_BILLING(0x26),
        LP_OF_PREVIOUS_BILLING(0x27),
        LP_ALL_DATA(0x52),

        UNKNOWN(0x00);

        private final int functionCode;

        private FunctionCode(int functionCode) {
            this.functionCode = functionCode;
        }

        public int getFunctionCode() {
            return functionCode;
        }

        public static FunctionCode fromCode(int code) {
            for (FunctionCode functionCode : FunctionCode.values()) {
                if (functionCode.getFunctionCode() == code) {
                    return functionCode;
                }
            }
            return FunctionCode.UNKNOWN;
        }
    }

    public static final int LENGTH = 1;

    private FunctionCode functionCode;

    public Function() {
        this.functionCode = FunctionCode.UNKNOWN;
    }

    public Function(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(functionCode.getFunctionCode(), LENGTH);
    }

    @Override
    public Function parse(byte[] rawData, int offset) throws ParsingException {
        int code = getIntFromBytesLE(rawData, offset, LENGTH);
        functionCode = FunctionCode.fromCode(code);
        if (functionCode.equals(FunctionCode.UNKNOWN)) {
            throw new ParsingException("Encountered invalid/unknown function code " + ProtocolTools.getHexStringFromInt(code, 1, "0x") + ".");
        }
        return this;
    }

    public static Function fromCode(int code) {
        FunctionCode theFunctionCode;
        for (FunctionCode functionCode : FunctionCode.values()) {
            if (functionCode.getFunctionCode() == code) {
                theFunctionCode = functionCode;
            }
        }
        theFunctionCode = FunctionCode.UNKNOWN;
        return new Function(theFunctionCode);
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }

    /**
     * Tests if the given {@link Function} is a regular one
     * (and thus belonging to a regular frame), or if it defines
     * a special command.
     *
     * @param function
     * @return true if the function is of regular type
     * false if the function defines a special command
     */
    public static boolean isRegularFunction(Function function) {
        if (function.getFunctionCode().equals(Function.FunctionCode.NACK) ||
                function.getFunctionCode().equals(Function.FunctionCode.WAIT)) {
            return false;
        }
        return true;
    }
}