/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterReading.java
 *
 * Created on 24 februari 2003, 10:24
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.cbo.Quantity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Koen
 */
public class MeterReadingData implements java.io.Serializable {

    List<com.energyict.protocol.RegisterValue> registerValues = new ArrayList<>(); // of type RegisterValue

    /**
     * Creates a new instance of MeterReading
     */
    public MeterReadingData() {
    }

    public void add(com.energyict.protocol.RegisterValue registerValue) {
        registerValues.add(registerValue);
    }

    /**
     * @deprecated replace by getRegisterValues()
     */
    public List<com.energyict.protocol.RegisterValue> getReadings() {
        return registerValues;
    }

    /**
     * @deprecated replace by setRegisterValues()
     */
    public void setReadings(List<com.energyict.protocol.RegisterValue> registerValues) {
        this.registerValues = registerValues;
    }

    /* backwards compatibility */
    public List<Quantity> getQuantities() {
        List<Quantity> result = new ArrayList<>(registerValues.size());
        for (com.energyict.protocol.RegisterValue registerValue : registerValues) {
            result.add(registerValue.getQuantity());
        }
        return result;
    }

    public List<com.energyict.protocol.RegisterValue> getRegisterValues() {
        return registerValues;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (com.energyict.protocol.RegisterValue registerValue : registerValues) {
            builder.append(registerValue.toString()).append("\n");
        }
        return builder.toString();
    }

}