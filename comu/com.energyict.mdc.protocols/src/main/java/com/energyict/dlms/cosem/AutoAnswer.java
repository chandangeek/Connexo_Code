/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.attributes.AutoAnswerAttributes;

import java.io.IOException;
import java.util.List;

public class AutoAnswer extends AbstractCosemObject {

    static final byte[] LN = new byte[]{0, 0, 2, 2, 0, (byte) 255};

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public AutoAnswer(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN, DLMSClassId.AUTO_CONNECT.getClassId()));
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.AUTO_CONNECT.getClassId();
    }

    /**
     * Write the given list of phone numbers to the list of allowed callers during the wake up.
     * Note that their type is always 1, indicating that these numbers are used for a wake up request.
     */
    public void writeListOfAllowedCallers(List<String> phoneNumbers) throws IOException {
        Array result = new Array();
        for (String phoneNumber : phoneNumbers) {
            Structure structure = new Structure();
            structure.addDataType(OctetString.fromString(phoneNumber));
            structure.addDataType(new TypeEnum(1));
            result.addDataType(structure);
        }
        write(AutoAnswerAttributes.LIST_OF_ALLOWED_CALLERS, result.getBEREncodedByteArray());
    }

    /**
     * Add the given list of phone numbers to the list of allowed callers during the wake up.
     * Note that their type is always 1, indicating that these numbers are used for a wake up request.
     */
    public void addListOfAllowedCallers(List<String> phoneNumbers) throws IOException {
        Array existingNumbers = readListOfAllowedCallers();
        for (String phoneNumber : phoneNumbers) {
            Structure structure = new Structure();
            structure.addDataType(OctetString.fromString(phoneNumber));
            structure.addDataType(new TypeEnum(1));
            existingNumbers.addDataType(structure);
        }
        write(AutoAnswerAttributes.LIST_OF_ALLOWED_CALLERS, existingNumbers.getBEREncodedByteArray());
    }

    public Array readListOfAllowedCallers() throws IOException {
        return readDataType(AutoAnswerAttributes.LIST_OF_ALLOWED_CALLERS, Array.class);
    }
}