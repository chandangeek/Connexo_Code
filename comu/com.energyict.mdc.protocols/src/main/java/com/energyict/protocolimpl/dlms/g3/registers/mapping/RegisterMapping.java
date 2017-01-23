package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:13 PM
 */
public abstract class RegisterMapping {

    private final CosemObjectFactory cosemObjectFactory;

    public RegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    /**
     * Check if a given {@link ObisCode} is readable by this register mapping.
     *
     * @param obisCode The {@link ObisCode} to validate
     * @return True if the value with the given {@link ObisCode} if mappable in this mapping
     */
    public abstract boolean canRead(final ObisCode obisCode);

    /**
     * Fetch the requested value from the meter and return it as a {@link com.energyict.mdc.protocol.api.device.data.RegisterValue}
     * If this method is called, the obisCode is guaranteed readable by this mapper,
     * so no extra validation of the obisCode is required in the implementation.
     *
     * @param obisCode The obisCode of the value to fetch
     * @return The register value
     * @throws java.io.IOException
     */
    protected abstract RegisterValue doReadRegister(final ObisCode obisCode) throws IOException;

    /**
     * Method used in the doReadRegister() part of this class, for parsing the AbstractDataType into a proper RegisterValue.
     * It is public to expose this functionality, so it can be used by other G3 protocols
     */
    public abstract RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException;

    /**
     * Try to read the value with the given ObisCode and return the result as a RegisterValue
     *
     * @param obisCode The {@link ObisCode} to fetch
     * @return The {@link com.energyict.mdc.protocol.api.device.data.RegisterValue}
     * @throws java.io.IOException
     */
    public RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (canRead(obisCode)) {
            return doReadRegister(obisCode);
        } else {
            throw new IOException("Register with obisCode [" + obisCode + "] is not supported by this register mapper [" + getClass().getName() + "]!");
        }
    }

    protected final CosemObjectFactory getCosemObjectFactory() {
        return this.cosemObjectFactory;
    }

    /**
     * Common method to shortly describe the elements of an array.
     * Each element is structure with some numerical values
     */
    protected String getShortDescription(Array array) {
        StringBuilder sb = new StringBuilder();
        for (AbstractDataType abstractStructure : array) {
            Structure structure = abstractStructure.getStructure();
            if (structure != null) {

                //Skip empty elements
                if (structure.getDataType(0).intValue() == 0xFFFF && structure.getDataType(1).intValue() == 0xFFFF) {
                    continue;
                }

                //Add valid elements to the StringBuilder
                for (int index = 0; index < structure.nrOfDataTypes(); index++) {
                    sb.append(index == 0 ? "" : ",");   //Separate values of the structure with a comma
                    String element = "";

                    AbstractDataType dataType = structure.getDataType(index);
                    if (dataType.isOctetString()) {
                        element = dataType.getOctetString().stringValue();
                    } else if (dataType.isBitString()) {
                        element = "0x" + Long.toHexString(dataType.getBitString().longValue());
                    } else {
                        element = String.valueOf(dataType.intValue());
                    }
                    sb.append(element);
                }
                sb.append(";");                             //Separate elements with a ;
            }
        }
        return sb.toString();
    }
}