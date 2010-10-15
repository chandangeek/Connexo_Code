package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private Qualifier totalizerQlf;
    private CTRAbstractValue<BigDecimal> totalizerValue;
    private CTRObjectID id;
    private PeriodTrace_C period;
    private ReferenceDate date;
    private List<AbstractCTRObject> traceData;

    public Trace_CQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        byte[] values = null;
        for (CTRAbstractValue<BigDecimal> value : dateAndhourS) {
            values = ProtocolTools.concatByteArrays(values, value.getBytes());
        }

        //Parse the bytes of the traces
        AttributeType type = new AttributeType(0x00);
        type.setHasIdentifier(false);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        byte[] traces = new byte[0];
        for (AbstractCTRObject ctrObject : traceData) {
            traces = ProtocolTools.concatByteArrays(traces, ctrObject.getBytes(type));
        }


        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                values,
                endOfDayTime.getBytes(),
                diagn.getBytes(),
                numberOfEvents.getBytes(),
                period.getBytes(),
                id.getBytes(),
                date.getBytes(),
                totalizerQlf.getBytes(),
                totalizerValue.getBytes(),
                traces
        ));
    }

    @Override
    public Trace_CQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0").getValue()[0];
        ptr += pdr.getValueLength();

        dateAndhourS = factory.parse(rawData, ptr, type, "8.0.1").getValue();
        ptr += sumLength(dateAndhourS);

        endOfDayTime = factory.parse(rawData, ptr, type, "8.1.3").getValue()[0];
        ptr += endOfDayTime.getValueLength();

        diagn = factory.parse(rawData, ptr, type, "12.6.3").getValue()[0];
        ptr += diagn.getValueLength();

        numberOfEvents = factory.parse(rawData, ptr, type, "10.1.0").getValue()[0];
        ptr += numberOfEvents.getValueLength();

        period = new PeriodTrace_C().parse(rawData, ptr);
        ptr += PeriodTrace_C.LENGTH;

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID().parse(b, 0);
        ptr += CTRObjectID.LENGTH;

        date = new ReferenceDate().parse(rawData, ptr);
        ptr += ReferenceDate.LENGTH;

        type.setHasQualifier(true);
        totalizerQlf = factory.parse(rawData, ptr, type, "2.0.0").getQlf();
        totalizerValue = factory.parse(rawData, ptr, type, "2.0.0").getValue()[0];
        ptr += totalizerQlf.LENGTH;
        ptr += totalizerValue.getValueLength();

        //Objects only have qlf and value fields, no ID.
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

    private int sumLength(CTRAbstractValue<BigDecimal>[] dateAndhourS) {
        int sumLength = 0;
        for (CTRAbstractValue<BigDecimal> value : dateAndhourS) {
            sumLength += value.getValueLength();
        }
        return sumLength;
    }


    public CTRAbstractValue<String> getPdr() {
        return pdr;
    }

    public void setPdr(CTRAbstractValue<String> pdr) {
        this.pdr = pdr;
    }

    public CTRAbstractValue<BigDecimal>[] getDateAndhourS() {
        return dateAndhourS;
    }

    public void setDateAndhourS(CTRAbstractValue<BigDecimal>[] dateAndhourS) {
        this.dateAndhourS = dateAndhourS;
    }

    public CTRAbstractValue<BigDecimal> getEndOfDayTime() {
        return endOfDayTime;
    }

    public void setEndOfDayTime(CTRAbstractValue<BigDecimal> endOfDayTime) {
        this.endOfDayTime = endOfDayTime;
    }

    public CTRAbstractValue<BigDecimal> getDiagn() {
        return diagn;
    }

    public void setDiagn(CTRAbstractValue<BigDecimal> diagn) {
        this.diagn = diagn;
    }

    public CTRAbstractValue<BigDecimal> getNumberOfEvents() {
        return numberOfEvents;
    }

    public void setNumberOfEvents(CTRAbstractValue<BigDecimal> numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public Qualifier getTotalizerQlf() {
        return totalizerQlf;
    }

    public void setTotalizerQlf(Qualifier totalizerQlf) {
        this.totalizerQlf = totalizerQlf;
    }

    public CTRAbstractValue<BigDecimal> getTotalizerValue() {
        return totalizerValue;
    }

    public void setTotalizerValue(CTRAbstractValue<BigDecimal> totalizerValue) {
        this.totalizerValue = totalizerValue;
    }

    public CTRObjectID getId() {
        return id;
    }

    public void setId(CTRObjectID id) {
        this.id = id;
    }

    public PeriodTrace_C getPeriod() {
        return period;
    }

    public void setPeriod(PeriodTrace_C period) {
        this.period = period;
    }

    public ReferenceDate getDate() {
        return date;
    }

    public void setDate(ReferenceDate date) {
        this.date = date;
    }

    public List<AbstractCTRObject> getTraceData() {
        return traceData;
    }

    public void setTraceData(List<AbstractCTRObject> traceData) {
        this.traceData = traceData;
    }

}