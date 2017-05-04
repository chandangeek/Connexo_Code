/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;

import java.io.IOException;
import java.util.List;

public class ProfileInfo {

    private int location;
    private int percent;
    private int savingEvent;
    private int profileIntervalIndex;
    private int[] gid = new int[4];
    private int[] registerAddress = new int[4];
    private int[] fieldAddress = new int[4];
    private int[] numberOfRegisters = new int[4];
    private int[] numberOfFields = new int[4];
    private int[] maxBytesPerField = new int[4];

    public ProfileInfo(List<ExtendedValue> values) throws IOException {
        int offset = 0;

        location = values.get(offset++).getValue();
        percent = values.get(offset++).getValue();
        savingEvent = values.get(offset++).getValue();
        profileIntervalIndex = values.get(offset++).getValue();

        for (int i = 0; i < 4; i++) {
            gid[i] = values.get(offset++).getValue();
            registerAddress[i] = values.get(offset++).getValue();
            fieldAddress[i] = values.get(offset++).getValue();
            numberOfRegisters[i] = values.get(offset++).getValue();
            numberOfFields[i] = values.get(offset++).getValue();
            maxBytesPerField[i] = values.get(offset++).getValue();
        }
    }

    public int[] getFieldAddress() {
        return fieldAddress;
    }

    public int[] getGid() {
        return gid;
    }

    public int getLocation() {
        return location;
    }

    public int[] getMaxBytesPerField() {
        return maxBytesPerField;
    }

    public int[] getNumberOfFields() {
        return numberOfFields;
    }

    public int[] getNumberOfRegisters() {
        return numberOfRegisters;
    }

    public int getPercent() {
        return percent;
    }

    public int getProfileIntervalIndex() {
        return profileIntervalIndex;
    }

    public int[] getRegisterAddress() {
        return registerAddress;
    }

    public int getSavingEvent() {
        return savingEvent;
    }
}