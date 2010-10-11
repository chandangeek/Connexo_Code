package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayQueryRequestStructure extends Data<ArrayQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private Index_Q index_Q;
    private Counter_Q counter_Q;
    private CTRObjectID id;

    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public ArrayQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += 6;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID(new String(b));
        ptr += CTRObjectID.LENGTH;

        index_Q = new Index_Q().parse(rawData, ptr);
        ptr += Index_Q.LENGTH;

        counter_Q = new Counter_Q().parse(rawData, ptr);
        ptr += Counter_Q.LENGTH;

        return super.parse(rawData, offset);
    }
}