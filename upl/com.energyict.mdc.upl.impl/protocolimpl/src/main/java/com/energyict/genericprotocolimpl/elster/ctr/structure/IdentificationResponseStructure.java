package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class IdentificationResponseStructure extends Data<IdentificationResponseStructure> {

    private CTRAbstractValue<String> pdrValue;
    private CTRAbstractValue<String>[] allPA;
    private CTRAbstractValue ncg;

    @Override
    public byte[] getBytes() {
        return super.getBytes();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public IdentificationResponseStructure parse(byte[] rawData, int offset) {
        int ptr = offset;

        try {
            CTRObjectFactory factory = new CTRObjectFactory();
            AttributeType valueAttributeType = new AttributeType();
            valueAttributeType.setHasValueFields(true);

            this.pdrValue = factory.parse(rawData, ptr, valueAttributeType, new CTRObjectID("C.0.0")).getValue()[0];
            ptr += 7; 

            // Unknown object???
            ptr += 4;

            this.allPA = factory.parse(rawData, ptr, valueAttributeType, new CTRObjectID("9.0.0")).getValue();
            ptr += 36;

            // Unknown object???
            ptr += 1;

            ptr += 1;

        } catch (CTRParsingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return super.parse(rawData, offset);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CTRAbstractValue getPdrValue() {
        return pdrValue;
    }
}
