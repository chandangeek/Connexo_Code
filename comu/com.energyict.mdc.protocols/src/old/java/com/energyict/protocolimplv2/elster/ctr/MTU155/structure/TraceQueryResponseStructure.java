package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfElements;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.StartDate;

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
            records = ProtocolTools.concatByteArrays(records, ctrObject.getBytes());
        }

        return padData(ProtocolTools.concatByteArrays(
                id.getBytes(),
                period.getBytes(),
                startDate.getBytes(),
                numberOfElements.getBytes(),
                records
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public TraceQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        int ptr = offset;

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        period = new PeriodTrace().parse(rawData, ptr);
        ptr += period.getLength();

        startDate = new StartDate().parse(rawData, ptr);
        ptr += startDate.getLength();

        numberOfElements = new NumberOfElements().parse(rawData, ptr);
        ptr += numberOfElements.getLength();

        //Objects only have qlf and value fields, no ID.
        AttributeType type = new AttributeType(0x00);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        type.setHasIdentifier(false);

        //Check the length of the objects by parsing the first object
        traceData = new ArrayList<AbstractCTRObject>();
        AbstractCTRObject obj = factory.parse(rawData, ptr, type, id.toString());
        traceData.add(obj);
        ptr += obj.getLength();

        //Parse the remaining objects
        while (ptr <= rawData.length - traceData.get(0).getBytes().length) {
            obj = factory.parse(rawData, ptr, type, id.toString());
            traceData.add(obj);
            ptr += obj.getBytes().length;
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