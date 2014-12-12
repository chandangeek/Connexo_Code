package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class AutomaticDemandResetCondition extends AbstractField<AutomaticDemandResetCondition> {

    public static final int LENGTH = 1;

    private int conditionCode;
    private ResetCondition demandResetCondition;

    public AutomaticDemandResetCondition() {
        this.demandResetCondition = ResetCondition.UNKNOWN;
    }

    public AutomaticDemandResetCondition(ResetCondition demandResetCondition) {
        this.demandResetCondition = demandResetCondition;
        this.conditionCode = demandResetCondition.getConditionCode();
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(conditionCode, LENGTH);
    }

    @Override
    public AutomaticDemandResetCondition parse(byte[] rawData, int offset) throws ParsingException {
        conditionCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        demandResetCondition = ResetCondition.fromConditionCode(conditionCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getConditionCode() {
        return conditionCode;
    }

    public String getDemandResetConditionMessage() {
        if (this.demandResetCondition.equals(ResetCondition.UNKNOWN)) {
            return (demandResetCondition.getMessage() + " " + conditionCode);
        }
        return demandResetCondition.getMessage();
    }

    public ResetCondition getDemandResetCondition() {
        return demandResetCondition;
    }

    public static AutomaticDemandResetCondition fromConditionCode(int conditionCode) {
        for (ResetCondition resetCondition : ResetCondition.values()) {
            if (resetCondition.getConditionCode() == conditionCode) {
                return new AutomaticDemandResetCondition(resetCondition);
            }
        }
        return new AutomaticDemandResetCondition(ResetCondition.UNKNOWN);
    }

    public enum ResetCondition {
        DISABLED(0, "Disabled"),
        ENABLED(1, "Enabled"),
        UNKNOWN(-1, "Unknown automatic demand reset condition");

        private final int conditionCode;
        private final String message;

        private ResetCondition(int conditionCode, String message) {
            this.conditionCode = conditionCode;
            this.message = message;
        }

        public int getConditionCode() {
            return conditionCode;
        }

        public String getMessage() {
            return message;
        }

        public static ResetCondition fromConditionCode(int conditionCode) {
            for (ResetCondition version : ResetCondition.values()) {
                if (version.getConditionCode() == conditionCode) {
                    return version;
                }
            }
            return ResetCondition.UNKNOWN;
        }
    }
}