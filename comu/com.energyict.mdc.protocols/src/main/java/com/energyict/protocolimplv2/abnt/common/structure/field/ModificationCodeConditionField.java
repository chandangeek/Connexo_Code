package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class ModificationCodeConditionField extends AbstractField<ModificationCodeConditionField> {

    public static final int LENGTH = 1;

    private int modificationCode;
    private ModificationCodeCondition modificationCodeCondition;

    public ModificationCodeConditionField() {
        this.modificationCodeCondition = ModificationCodeCondition.UNKNOWN;
    }

    public ModificationCodeConditionField(ModificationCodeCondition modificationCodeCondition) {
        this.modificationCodeCondition = modificationCodeCondition;
        this.modificationCode = modificationCodeCondition.getModificationCode();
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(modificationCode, LENGTH);
    }

    @Override
    public ModificationCodeConditionField parse(byte[] rawData, int offset) throws ParsingException {
        modificationCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        modificationCodeCondition = ModificationCodeCondition.fromModificationCode(modificationCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getModificationCode() {
        return modificationCode;
    }

    public String getModificationCodeMessage() {
        if (!this.modificationCodeCondition.equals(ModificationCodeCondition.UNKNOWN)) {
            return modificationCodeCondition.getMessage();
        } else {
            return (modificationCodeCondition.getMessage() + " " + modificationCode);
        }
    }

    public ModificationCodeCondition getModificationCodeCondition() {
        return modificationCodeCondition;
    }

    public enum ModificationCodeCondition {
        MODIFICATION(0, "Modification command"),
        READING(1, "Reading command"),
        UNKNOWN(-1, "Unknown modification code");

        private final int modificationCode;
        private final String message;

        private ModificationCodeCondition(int modificationCode, String message) {
            this.modificationCode = modificationCode;
            this.message = message;
        }

        public int getModificationCode() {
            return modificationCode;
        }

        public String getMessage() {
            return message;
        }

        public static ModificationCodeCondition fromModificationCode(int statusCode) {
            for (ModificationCodeCondition version : ModificationCodeCondition.values()) {
                if (version.getModificationCode() == statusCode) {
                    return version;
                }
            }
            return ModificationCodeCondition.UNKNOWN;
        }
    }
}