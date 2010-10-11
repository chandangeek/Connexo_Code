package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.NumberOfObjects;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 9:50:05
 */
public class RegisterQueryResponseStructure extends Data<RegisterQueryResponseStructure> {

    private AbstractCTRObject[] objects;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;

    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public RegisterQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();

        int ptr = offset;
        
        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += NumberOfObjects.LENGTH;

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        for (int i = 0; i < numberOfObjects.getNumberOfObjects(); i++) {
            objects[i] = factory.parse(rawData, ptr, attributeType);
            ptr += 2 * objects[i].parseValueLengths(objects[i].getId())[0];
            ptr += 4;  //The length of the id, the qlf and the access bytes
        }

        return super.parse(rawData, offset);
    }

}
