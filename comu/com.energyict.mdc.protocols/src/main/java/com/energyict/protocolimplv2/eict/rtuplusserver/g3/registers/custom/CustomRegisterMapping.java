package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Custom registers mappers return a register value that describes all the attributes of an abstract COSEM object.
 * Copyrights EnergyICT
 * Date: 15/05/14
 * Time: 15:37
 * Author: khe
 */
public abstract class CustomRegisterMapping {

    protected CosemObjectFactory cosemObjectFactory;

    public abstract RegisterValue readRegister() throws IOException;

    public abstract ObisCode getObisCode();

    protected RegisterValue createAttributesOverview(AbstractDataType... abstractDataTypes) {
        return createAttributesOverview(false, abstractDataTypes);
    }

    protected RegisterValue createAttributesOverview(boolean hex, AbstractDataType... abstractDataTypes) {
        StringBuilder result = new StringBuilder();

        boolean addSeparator = false;
        for (AbstractDataType abstractDataType : abstractDataTypes) {
            if (addSeparator) { //Don't add this for the very first element
                result.append(";"); //Attributes are separated by a semicolon
            }
            if (abstractDataType.isStructure()) {
                result.append(getStructureDescription(abstractDataType.getStructure(), hex));
            } else {
                result.append(getDataTypeDescription(abstractDataType, hex));
            }
            addSeparator = true;
        }
        return new RegisterValue(getObisCode(), result.toString());
    }

    /**
     * Common method to shortly describe the elements of a structure
     */
    protected String getStructureDescription(Structure structure, boolean hex) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < structure.nrOfDataTypes(); index++) {
            result.append(index == 0 ? "" : ",");   //Separate values of the structure with a comma
            AbstractDataType dataType = structure.getDataType(index);
            result.append(getDataTypeDescription(dataType, hex));
        }
        return "{" + result.toString() + "}";
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    private String getDataTypeDescription(AbstractDataType dataType, boolean hex) {
        if (dataType.isOctetString()) {
            if (hex) {
                return ProtocolTools.getHexStringFromBytes(dataType.getOctetString().getOctetStr(), "");
            } else {
                return dataType.getOctetString().stringValue();
            }
        } else if (dataType.isBitString()) {
            return "0x" + Long.toHexString(dataType.getBitString().longValue());
        } else {
            return String.valueOf(dataType.intValue());
        }
    }
}