package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 * Created by cisac on 8/4/2016.
 */
public class GeneralLocalPortReadout extends AbstractCosemObject {
    public static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.21.0.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public GeneralLocalPortReadout(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PROFILE_GENERIC.getClassId();
    }

    public void writePushObjectList(List<ObjectDefinition> objectDefinitionList) throws IOException {
        Array objectDefinitions = new Array();
        for (ObjectDefinition objectDefinition : objectDefinitionList) {
            Structure structure = new Structure();
            structure.addDataType(new Unsigned16(objectDefinition.getClassId()));
            structure.addDataType(OctetString.fromObisCode(objectDefinition.getObisCode()));
            structure.addDataType(new Integer8(objectDefinition.getAttributeIndex()));
            structure.addDataType(new Unsigned16(objectDefinition.getDataIndex()));
            objectDefinitions.addDataType(structure);
        }
        write(3, objectDefinitions.getBEREncodedByteArray());
    }
}
