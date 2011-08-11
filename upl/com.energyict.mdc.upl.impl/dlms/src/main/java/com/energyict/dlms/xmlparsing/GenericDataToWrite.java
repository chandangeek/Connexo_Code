package com.energyict.dlms.xmlparsing;

import com.energyict.dlms.cosem.GenericWrite;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:34:36
 */
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
}
