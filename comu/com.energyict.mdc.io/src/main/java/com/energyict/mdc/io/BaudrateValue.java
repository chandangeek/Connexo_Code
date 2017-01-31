/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import com.energyict.mdc.common.ApplicationException;

import Serialio.SerialConfig;

import java.math.BigDecimal;

/**
 * Provide predefined values for the used BaudRate.
 */
public enum BaudrateValue {
    BAUDRATE_150(150) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_150;
        }
    },
    BAUDRATE_300(300) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_300;
        }
    },
    BAUDRATE_600(600) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_600;
        }
    },
    BAUDRATE_1200(1200) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_1200;
        }
    },
    BAUDRATE_1800(1800),
    BAUDRATE_2400(2400) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_2400;
        }
    },
    BAUDRATE_4800(4800) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_4800;
        }
    },
    BAUDRATE_7200(7200),
    BAUDRATE_9600(9600) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_9600;
        }
    },
    BAUDRATE_14400(14400),
    BAUDRATE_19200(19200) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_19200;
        }
    },
    BAUDRATE_28800(28800),
    BAUDRATE_38400(38400) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_38400;
        }
    },
    BAUDRATE_56000(56000),
    BAUDRATE_57600(57600) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_57600;
        }
    },
    BAUDRATE_76800(76800),
    BAUDRATE_115200(115200) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_115200;
        }
    },
    BAUDRATE_230400(230400) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_230400;
        }
    },
    BAUDRATE_460800(460800) {
        @Override
        public int sioBaudRate () {
            return SerialConfig.BR_460800;
        }
    };

    private final BigDecimal baudrate;

    BaudrateValue(int baudrate) {
        this.baudrate = new BigDecimal(baudrate);
    }

    public static BigDecimal[] getTypedValues() {
        BigDecimal[] typedValues = new BigDecimal[values().length];
        int i = 0;
        for (BaudrateValue baudrate : values()) {
            typedValues[i] = baudrate.value();
            i++;
        }
        return typedValues;
    }

    public static BaudrateValue valueFor (BigDecimal numercialValue) {
        for (BaudrateValue baudrateValue : values()) {
            if (baudrateValue.value().equals(numercialValue)) {
                return baudrateValue;
            }
        }
        return null;
    }

    public static int getSioBaudrateFor (BigDecimal baudrate) {
        BaudrateValue baudrateValue = valueFor(baudrate);
        if (baudrateValue == null) {
            throw new ApplicationException("Baudrate " + baudrate + " is not supported by this driver.");
        }
        else {
            try {
                return baudrateValue.sioBaudRate();
            }
            catch (NotSupportedBySio e) {
                throw new ApplicationException("Baudrate " + baudrate + " is not supported by this driver.");
            }
        }
    }

    public BigDecimal value() {
        return baudrate;
    }

    public int sioBaudRate () {
        throw new NotSupportedBySio();
    }

    @Override
    public String toString() {
        return String.valueOf(value().intValue());
    }

    private class NotSupportedBySio extends RuntimeException {
    }

}