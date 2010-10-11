package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class TraceQueryResponseStructure extends Data<TraceQueryResponseStructure> {

    private Period period;
    private StartDate startDate;
    private CTRObjectID id;
    private Element elements;
    private DataArray traceData;

    @Override
    public byte[] getBytes() {
        return super.getBytes();
    }

    @Override
    public TraceQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID(new String(b));
        ptr += CTRObjectID.LENGTH;

        period = new Period().parse(rawData, ptr);
        ptr += Period.LENGTH;

        startDate = new StartDate().parse(rawData, ptr);
        ptr += StartDate.LENGTH;

        elements = new Element().parse(rawData, ptr);
        ptr += Element.LENGTH;

        traceData = new DataArray().parse(rawData, ptr);


        return super.parse(rawData, offset);
    }
}