package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.GenericPlcIBSetupAttributes;
import com.energyict.dlms.cosem.methods.GenericPlcIBSetupMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 3/4/13
 * Time: 10:04 AM
 */
public class GenericPlcIBSetup extends AbstractCosemObject {

    /**
     * The default obis code
     */
    private static final ObisCode LN = ObisCode.fromString("0.0.128.0.3.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public GenericPlcIBSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.GENERIC_PLC_IB_SETUP.getClassId();
    }

    /**
     * @return The default obis code of this cosem object
     */
    public static ObisCode getDefaultObisCode() {
        return LN;
    }

    /**
     * @param ids The id's to read or none to read them all
     * @return
     * @throws java.io.IOException
     */
    public final Array readRawIBValues(final int... ids) throws IOException {
        final Array attributes;
        if (ids != null && ids.length > 0) {
            attributes = new Array();
            for (final int attributeId : ids) {
                attributes.addDataType(new Unsigned32(attributeId));
            }
        } else {
            attributes = null;
        }

        return readDataType(GenericPlcIBSetupAttributes.READ_RAW_IB, attributes, Array.class);
    }

    /**
     * @param values The values to write to the device
     * @return The results of the write action
     */
    public final Array writeRawIBValues(final Array values) throws IOException {
        final byte[] result = this.methodInvoke(GenericPlcIBSetupMethods.WRITE_RAW_IB, values);
        return AXDRDecoder.decode(result, Array.class);
    }

}
