/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.SpecialDaysTableAttributes;
import com.energyict.dlms.cosem.methods.SpecialDaysTableMethods;

import java.io.IOException;

/**
 * A straightforward implementation of the SpecialDaysTable object according to the DLMS BlueBooks
 */
public class SpecialDaysTable extends AbstractCosemObject {

    /**
     * The array containing the specialDayEntry Structures
     */
    private Array specialDays = null;

    public SpecialDaysTable(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * {@inheritDoc}
     */
    protected int getClassId() {
        return DLMSClassId.SPECIAL_DAYS_TABLE.getClassId();
    }

    /**
     * Write the given SpecialDay array to the device
     *
     * @param specialDays the given special day array
     * @throws java.io.IOException if the write has failed
     */
    public void writeSpecialDays(Array specialDays) throws IOException {
        write(SpecialDaysTableAttributes.ENTRIES, specialDays.getBEREncodedByteArray());
        this.specialDays = specialDays;
    }

    /**
     * Read the specialDayTable Array from the device
     *
     * @return the requested SpecialDay table
     * @throws java.io.IOException if the read has failed.
     */
    public Array readSpecialDays() throws IOException {
        if (specialDays == null) {
            specialDays = (Array) AXDRDecoder.decode(getResponseData(SpecialDaysTableAttributes.ENTRIES));
        }
        return specialDays;
    }

    /**
     * Insert a specialDay Entry to the list of SpecialDays
     *
     * @param structure the structure containing the SpecialDayEntry definition
     * @throws java.io.IOException if the write has failed
     */
    public void insert(Structure structure) throws IOException {
        methodInvoke(SpecialDaysTableMethods.INSERT, structure);
    }

    /**
     * Delete a specialDay entry from the list of SpecialDays
     *
     * @param index the index to delete
     * @throws java.io.IOException if the meter reported an exception(eg. index out of bounds) or if the invocation failed
     */
    public void delete(int index) throws IOException {
        Unsigned16 u16 = new Unsigned16(index);
        methodInvoke(SpecialDaysTableMethods.DELETE, u16);
    }

}
