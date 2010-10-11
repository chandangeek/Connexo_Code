package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class Trace_CQueryResponseStructure extends Data<Trace_CQueryResponseStructure> {

    private CTRAbstractValue<String> pdr;
    private CTRAbstractValue<BigDecimal>[] dateAndhourS;
    private CTRAbstractValue<BigDecimal> endOfDayTime;
    private CTRAbstractValue<BigDecimal> diagn;
    private CTRAbstractValue<BigDecimal> numberOfEvents;
    private CTRObjectID totalizerId;
    private CTRAbstractValue<BigDecimal> totalizerValue;
    private CTRObjectID id;
    private PeriodTrace_C period;
    private ReferenceDate date;

    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public Trace_CQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0").getValue()[0];
        ptr += 7;

        dateAndhourS = factory.parse(rawData, ptr, type, "8.0.1").getValue();
        ptr += 5;

        endOfDayTime = factory.parse(rawData, ptr, type, "8.1.3").getValue()[0];
        ptr += 1;

        diagn = factory.parse(rawData, ptr, type, "12.6.3").getValue()[0];
        ptr += 2;

        numberOfEvents = factory.parse(rawData, ptr, type, "10.1.0").getValue()[0];
        ptr += 2;

        period = new PeriodTrace_C().parse(rawData, ptr);
        ptr += PeriodTrace_C.LENGTH;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID(new String(b));
        ptr += CTRObjectID.LENGTH;

        date = new ReferenceDate().parse(rawData, ptr);
        ptr += ReferenceDate.LENGTH;

        type.setHasQualifier(true);
        totalizerId = factory.parse(rawData, ptr, type, "2.0.0").getId();
        totalizerValue = factory.parse(rawData, ptr, type, "2.0.0").getValue()[0];
        ptr += 5;
        
        
        return super.parse(rawData, offset);
    }
}