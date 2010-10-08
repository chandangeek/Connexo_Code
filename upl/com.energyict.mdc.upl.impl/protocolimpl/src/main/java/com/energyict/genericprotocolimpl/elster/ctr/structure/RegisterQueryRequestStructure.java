package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.NumberOfObjects;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class RegisterQueryRequestStructure extends Data<RegisterQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private NumberOfObjects numberOfObjects;
    private AttributeType attributeType;

    @Override
    public byte[] getBytes() {
        return super.getBytes();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public RegisterQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += 6;

        numberOfObjects = new NumberOfObjects().parse(rawData, ptr);
        ptr += NumberOfObjects.LENGTH;

        this.attributeType = new AttributeType().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        return super.parse(rawData, offset);
    }
}
