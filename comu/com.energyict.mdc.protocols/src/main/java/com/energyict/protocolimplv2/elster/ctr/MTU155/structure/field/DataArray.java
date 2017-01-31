/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class DataArray extends AbstractField<DataArray> {

    private byte[] data;
    private int arrayLength;

    public DataArray(int arrayLength) {
        this.arrayLength = arrayLength;
        this.data = new byte[arrayLength];
    }

    public int getArrayLength() {
        return arrayLength;
    }

    public void setArrayLength(int arrayLength) {
        this.arrayLength = arrayLength;
        this.data = new byte[arrayLength];
    }
    
    public byte[] getBytes() {
        return data;
    }

    public DataArray parse(byte[] rawData, int offset) throws CTRParsingException {
        data = ProtocolTools.getSubArray(rawData, offset, offset + arrayLength);
        return this;
    }

    public int getLength() {
        return arrayLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data.clone();
        this.arrayLength = data.length;
    }
    
}
