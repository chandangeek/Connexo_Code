/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.energyict.mdc.device.data.Device;

import java.math.BigDecimal;


public enum DeviceAttribute {

    SERIAL_NUMBER {
        @Override
        public String getName() {
            return "Serial number";
        }

        @Override
        public void setValueToObject(Device device, Object value) {
            device.setSerialNumber(value.toString());
        }
    },

    DEVICE_NAME {
        @Override
        public String getName() {
            return "Name";
        }

        @Override
        public void setValueToObject(Device device, Object value) {
            device.setName(value.toString());
        }
    },

    MULTIPLIER {
        @Override
        public String getName() {
            return "Multiplier";
        }

        @Override
        public void setValueToObject(Device device, Object value) {
            device.setMultiplier((BigDecimal) value);
        }
    };

    public abstract String getName();
    public abstract void setValueToObject(Device device, Object value);
}
