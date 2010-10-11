package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayQueryResponseStructure extends Data<ArrayQueryResponseStructure> {

    private Index_Q index_A;
    private Counter_Q counter_A;
    private CTRObjectID id;
    private AttributeType attributeType;
    private Coda coda;
    private DataArray data;
               
    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public ArrayQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID(new String(b));
        ptr += CTRObjectID.LENGTH;

        attributeType = new AttributeType().parse(rawData, ptr);
        ptr += AttributeType.LENGTH;

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += Index_Q.LENGTH;

        counter_A = new Counter_Q().parse(rawData, ptr);
        ptr += Counter_Q.LENGTH;

        coda = new Coda().parse(rawData, ptr);
        ptr += Coda.LENGTH;

        data = new DataArray().parse(rawData, ptr);

        return super.parse(rawData, offset);
    }
}