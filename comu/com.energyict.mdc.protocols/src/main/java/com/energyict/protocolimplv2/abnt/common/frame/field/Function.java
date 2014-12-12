package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class Function extends AbstractField<Function> {

    public static enum FunctionCode {
        ENQ(0x05, FrameFormat.SINGLE_RESPONSE),
        ACK(0x06, FrameFormat.SINGLE_RESPONSE),
        NACK(0x15, FrameFormat.SINGLE_RESPONSE),
        WAIT(0x10, FrameFormat.SINGLE_RESPONSE),
        INSTRUMENTATION_PAGE(0x14, FrameFormat.SINGLE_RESPONSE),
        ACTUAL_PARAMETERS_WITH_DEMAND_RESET(0x20, FrameFormat.SINGLE_RESPONSE),
        ACTUAL_PARAMETERS(0x21, FrameFormat.SINGLE_RESPONSE),
        PREVIOUS_PARAMETERS(0x22, FrameFormat.SINGLE_RESPONSE),
        ACTUAL_PARAMETERS_WITH_SELECTOR(0x51, FrameFormat.SINGLE_RESPONSE),
        CURRENT_REGISTERS(0x23, FrameFormat.SINGLE_RESPONSE),
        PREVIOUS_REGISTERS(0x24, FrameFormat.SINGLE_RESPONSE),
        POWER_FAIL_LOG(0x25, FrameFormat.SINGLE_RESPONSE),
        LP_OF_CURRENT_BILLING(0x26, FrameFormat.SEGMENTED_RESPONSE),
        LP_OF_PREVIOUS_BILLING(0x27, FrameFormat.SEGMENTED_RESPONSE),
        HISTORY_LOG(0x28, FrameFormat.SINGLE_RESPONSE),
        DATE_CHANGE(0x29, FrameFormat.SINGLE_RESPONSE),
        TIME_CHANGE(0x30, FrameFormat.SINGLE_RESPONSE),
        CONFIGURE_HOLIDAY_LIST(0x32, FrameFormat.SINGLE_RESPONSE),
        LP_DATA_WITH_SELECTOR(0x52, FrameFormat.SEGMENTED_RESPONSE),    // Warning: should not be used, cause doesn't handle time gaps correct
        CONFIGURE_AUTOMATIC_DEMAND_RESET(0x63, FrameFormat.SINGLE_RESPONSE),
        CONFIGURE_DST(0x64, FrameFormat.SINGLE_RESPONSE),
        READ_INSTALLATION_CODE(0x87, FrameFormat.SINGLE_RESPONSE),
        UNKNOWN(0x00, FrameFormat.SINGLE_RESPONSE);

        private final int functionCode;
        private final FrameFormat frameFormat;

        private FunctionCode(int functionCode, FrameFormat frameFormat) {
            this.functionCode = functionCode;
            this.frameFormat = frameFormat;
        }

        public int getFunctionCode() {
            return functionCode;
        }

        public FrameFormat getFrameFormat() {
            return frameFormat;
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

    public enum FrameFormat {
        SINGLE_RESPONSE,
        SEGMENTED_RESPONSE // The response is segmented over multiple frames (e.g. load profile readout)
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
        return this;
    }

    public static Function fromCode(int code) {
        for (FunctionCode functionCode : FunctionCode.values()) {
            if (functionCode.getFunctionCode() == code) {
                return new Function(functionCode);
            }
        }
        return new Function(FunctionCode.UNKNOWN);
    }

    public static Function fromFunctionCode(FunctionCode functionCode) {
        return new Function(functionCode);
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
     * @param function the Function to examine
     * @return true if the function is of regular type
     * false if the function defines a special command
     */
    public static boolean isRegularFunction(Function function) {
        return !(function.getFunctionCode().equals(FunctionCode.ENQ) ||
                function.getFunctionCode().equals(FunctionCode.ACK) ||
                function.getFunctionCode().equals(FunctionCode.NACK) ||
                function.getFunctionCode().equals(FunctionCode.WAIT));
    }

    /**
     * Tests if the given {@link Function} is the heartbeat function or not
     *
     * @param function the Function to examine
     * @return true if the function is the heartbeat function
     */
    public static boolean isHeartBeatFunction(Function function) {
        return function.getFunctionCode().equals(FunctionCode.ENQ);
    }

    /**
     * Tests if the given {@link Function} allows segmented response
     *
     * @param function the Function to examine
     * @return true if the function allows segmented response
     */
    public static boolean allowsSegmentation(Function function) {
        return function.getFunctionCode().getFrameFormat().equals(FrameFormat.SEGMENTED_RESPONSE);
    }
}