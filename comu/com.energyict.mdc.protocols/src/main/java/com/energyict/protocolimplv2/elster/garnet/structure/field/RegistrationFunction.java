/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class RegistrationFunction extends AbstractField<RegistrationFunction> {

    public static final int LENGTH = 1;

    private int functionId;
    private Function function;

    public RegistrationFunction() {
        this.function = Function.UNKNOWN;
    }

    public RegistrationFunction(Function function) {
        this.function = function;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(functionId, LENGTH);
    }

    @Override
    public RegistrationFunction parse(byte[] rawData, int offset) throws ParsingException {
        functionId = getIntFromBytesLE(rawData, offset, LENGTH);
        function = Function.fromCode(functionId);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getFunctionId() {
        return functionId;
    }

    public String getFunctionInfo() {
        if (!this.function.equals(Function.UNKNOWN)) {
            return function.getFunctionDescription();
        } else {
            return (function.getFunctionDescription() + " " + functionId);
        }
    }

    private enum Function {

        UNREGISTERED(0x00, "Unregistered"),
        REGISTERED(0x01, "Registered"),
        UNKNOWN(0xFF, "Unknown function state");

        /**
         * The error code of the error *
         */
        private final int registerCode;

        /**
         * The textual description of the error *
         */
        private final String registerDescription;

        private Function(int registerCode, String registerDescription) {
            this.registerCode = registerCode;
            this.registerDescription = registerDescription;
        }

        public int getRegisterCode() {
            return registerCode;
        }

        public String getFunctionDescription() {
            return registerDescription;
        }

        public static Function fromCode(int code) {
            for (Function function : Function.values()) {
                if (function.getRegisterCode() == code) {
                    return function;
                }
            }
            return Function.UNKNOWN;
        }
    }
}