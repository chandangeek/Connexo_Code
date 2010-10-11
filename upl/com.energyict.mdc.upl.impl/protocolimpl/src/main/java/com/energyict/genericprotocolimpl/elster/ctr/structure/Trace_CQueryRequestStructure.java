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
public class Trace_CQueryRequestStructure extends Data<Trace_CQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private CTRObjectID id;
    private PeriodTrace_C period;
    private ReferenceDate date;

    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public Trace_CQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += 6;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID(new String(b));
        ptr += CTRObjectID.LENGTH;

        period = new PeriodTrace_C().parse(rawData, ptr);
        ptr += PeriodTrace_C.LENGTH;

        date = new ReferenceDate().parse(rawData, ptr);
        ptr += ReferenceDate.LENGTH;

        

        
        return super.parse(rawData, offset);
    }
}