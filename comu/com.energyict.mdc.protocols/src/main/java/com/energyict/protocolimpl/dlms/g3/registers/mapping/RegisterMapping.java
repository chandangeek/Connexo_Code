package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 7/12/12
 * Time: 2:13 PM
 */
public abstract class RegisterMapping {

    private final DlmsSession session;

    protected RegisterMapping(final DlmsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("The DlmsSession object is required for each RegisterMapping, but was 'null'!");
        }
        this.session = session;
    }

    /**
     * Check if a given {@link com.energyict.obis.ObisCode} is readable by this register mapping.
     *
     * @param obisCode The {@link com.energyict.obis.ObisCode} to validate
     * @return True if the value with the given {@link com.energyict.obis.ObisCode} if mappable in this mapping
     */
    public abstract boolean canRead(final ObisCode obisCode);

    /**
     * Fetch the requested value from the meter and return it as a {@link com.energyict.protocol.RegisterValue}
     * If this method is called, the obisCode is guaranteed readable by this mapper,
     * so no extra validation of the obisCode is required in the implementation.
     *
     * @param obisCode The obisCode of the value to fetch
     * @return The register value
     * @throws java.io.IOException
     */
    protected abstract RegisterValue doReadRegister(final ObisCode obisCode) throws IOException;

    /**
     * Try to read the value with the given {@link com.energyict.obis.ObisCode} and return the result as a {@link com.energyict.protocol.RegisterValue}
     *
     * @param obisCode The {@link com.energyict.obis.ObisCode} to fetch
     * @return The {@link com.energyict.protocol.RegisterValue}
     * @throws java.io.IOException
     */
    public RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        if (canRead(obisCode)) {
            return doReadRegister(obisCode);
        } else {
            throw new IOException("Register with obisCode [" + obisCode + "] is not supported by this register mapper [" + getClass().getName() + "]!");
        }
    }

    protected final Logger getLogger() {
        return this.session.getLogger();
    }

    protected final CosemObjectFactory getCosemObjectFactory() {
        return this.session.getCosemObjectFactory();
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
                    sb.append(structure.getDataType(index).intValue());
                }
                sb.append(";");                             //Separate elements with a ;
            }
        }
        return sb.toString();
    }
}