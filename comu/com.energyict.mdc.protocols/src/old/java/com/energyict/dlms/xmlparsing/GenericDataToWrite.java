/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.xmlparsing;

import com.energyict.dlms.cosem.GenericWrite;

import java.io.IOException;

public class GenericDataToWrite {

    private final GenericWrite genericWrite;
    private final byte[] dataToWrite;

    public GenericDataToWrite(final GenericWrite genericWrite, final byte[] dataToWrite) {
        this.genericWrite = genericWrite;
        this.dataToWrite = dataToWrite.clone();
    }

    public GenericWrite getGenericWrite() {
        return genericWrite;
    }

    public byte[] getDataToWrite() {
        return dataToWrite;
    }

    /**
     * Write data to device
     *
     * @throws IOException if something happened during the write
     */
    public void writeData() throws IOException {
        this.genericWrite.write(dataToWrite);
    }
}
