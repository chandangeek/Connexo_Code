/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class TariffConfigurationField extends AbstractField<TariffConfigurationField> {

    public static final int LENGTH = 1;

    private int code;
    private TariffCode tariffCode;

    public TariffConfigurationField() {
        code = 0;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(code, LENGTH);
    }

    @Override
    public TariffConfigurationField parse(byte[] rawData, int offset) throws ParsingException {
        code = getIntFromBytes(rawData, offset, LENGTH);
        tariffCode = TariffCode.tariffCodeFromCode(code);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getCode() {
        return code;
    }

    public String getTariffInfo() {
        return tariffCode.getMessage();
    }

    public TariffCode getTariffCode() {
        return tariffCode;
    }

    public enum TariffCode {
        UNDEFINED(0, "Undefined, it counts only Off-Peak"),
        PEAK_AND_OFF_PEAK(1, "Peak and Off-Peak"),
        ONLY_OFF_PEAK(2, "Only Off-Peak"),
        PEAK_AND_OFF_PEAK_PREF(3, "Peak and Off-Peak"),
        OFF_PEAK_AND_RESERVED(4, "Off-Peak and Reserved"),
        PEAK_OFF_PEAK_AND_RESERVED(5, "Peak, Off-Peak and Reserved"),
        OFF_PEAK_AND_RESERVED_PREF(6, "Off-Peak and Reserved"),
        PEAK_OFF_PEAK_AND_RESERVED_PREF(7, "Peak, Off-Peak and Reserved");

        private final int code;
        private final String message;


        TariffCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static TariffCode tariffCodeFromCode(int code) {
            for (TariffCode tariffCode : values()) {
                if (tariffCode.getCode() == code) {
                    return tariffCode;
                }
            }
            return UNDEFINED;
        }
    }
}