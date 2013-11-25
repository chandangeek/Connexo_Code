package com.energyict.protocolimpl.modbus.core;

import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCode;

import java.io.IOException;

/**
 * @author Koen
 */
public class ModbusException extends IOException {

    private int functionErrorCode;
    private ModbusExceptionCode exceptionCode;

    public ModbusException(String str) {
        super(str);
    }

    public ModbusException(int functionErrorCode, int exceptionCode) {
        super(constructErrorMessage(functionErrorCode, exceptionCode));
        this.functionErrorCode = functionErrorCode;
        this.exceptionCode = ModbusExceptionCode.byResultCode(exceptionCode);
    }

    public int getFunctionErrorCode() {
        return functionErrorCode;
    }

    public ModbusExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public boolean isRetryAllowed() {
        return getExceptionCode() == null
                ? true
                : getExceptionCode().isRetryAllowed();
    }

    private static String constructErrorMessage(int functionErrorCode, int exceptionCode) {
        int functionCode = functionErrorCode % 0x80;

        StringBuilder builder = new StringBuilder();
        builder.append("Received exception response ");
        builder.append(String.format("0x%02X", exceptionCode));
        builder.append(" (");
        builder.append(getModbusExceptionCodeDescription(exceptionCode));
        builder.append(") in response to request ");
        builder.append(String.format("0x%02X", functionCode));
        builder.append(" (");
        builder.append(getFunctionCodeDescription(functionCode));
        builder.append(")");
        return builder.toString();
    }

    private static String getModbusExceptionCodeDescription(int val) {
        if (ModbusExceptionCode.byResultCode(val) != null) {
            return ModbusExceptionCode.byResultCode(val).getDescription();
        } else {
            return "Unknown Modbus exception code";
        }
    }

    private static String getFunctionCodeDescription(int val) {
        if (FunctionCode.byFunctionCode(val) != null) {
            return FunctionCode.byFunctionCode(val).getDescription();
        } else {
            return "Unknown function code";
        }
    }
}
