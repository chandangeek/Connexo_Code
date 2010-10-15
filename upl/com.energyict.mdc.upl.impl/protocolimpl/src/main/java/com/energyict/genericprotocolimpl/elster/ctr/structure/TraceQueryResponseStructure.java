package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class TraceQueryResponseStructure extends Data<TraceQueryResponseStructure> {

    private PeriodTrace period;
    private StartDate startDate;
    private CTRObjectID id;
    private NumberOfElements numberOfElements;
    private List<AbstractCTRObject> traceData;

    public TraceQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {

        AttributeType type = new AttributeType(0x00);
        type.setHasIdentifier(false);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        byte[] records = new byte[0];
        for (AbstractCTRObject ctrObject : traceData) {
            records = ProtocolTools.concatByteArrays(records, ctrObject.getBytes(type));
        }

        return padData(ProtocolTools.concatByteArrays(
                id.getBytes(),
                period.getBytes(),
                startDate.getBytes(),
                numberOfElements.getBytes(),
                records
        ));
    }

    @Override
    public TraceQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        int ptr = offset;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID().parse(b, 0);
        ptr += CTRObjectID.LENGTH;

        period = new PeriodTrace().parse(rawData, ptr);
        ptr += PeriodTrace.LENGTH;

        startDate = new StartDate().parse(rawData, ptr);
        ptr += StartDate.LENGTH;

        numberOfElements = new NumberOfElements().parse(rawData, ptr);
        ptr += NumberOfElements.LENGTH;

        //Objects only have qlf and value fields, no ID.
        AttributeType type = new AttributeType(0x00);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        type.setHasIdentifier(false);

        //Check the length of the objects by parsing the first object
        traceData = new ArrayList<AbstractCTRObject>();
        AbstractCTRObject obj = factory.parse(rawData, ptr, type, id.toString());
        traceData.add(obj);
        ptr += obj.getLength(type);

        //Parse the remaining objects
        while (ptr <= rawData.length - traceData.get(0).getBytes(type).length) {
            obj = factory.parse(rawData, ptr, type, id.toString());
            traceData.add(obj);
            ptr += obj.getBytes(type).length;
        }

        return this;
    }

    public PeriodTrace getPeriod() {
        return period;
    }

    public void setPeriod(PeriodTrace period) {
        this.period = period;
    }

    public StartDate getStartDate() {
        return startDate;
    }

    public void setStartDate(StartDate startDate) {
        this.startDate = startDate;
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }

    public NumberOfElements getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(NumberOfElements numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public List<AbstractCTRObject> getTraceData() {
        return traceData;
    }

    public void setTraceData(List<AbstractCTRObject> traceData) {
        this.traceData = traceData;
    }
}