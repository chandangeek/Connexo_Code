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

    private PeriodTrace period;
    private StartDate startDate;
    private CTRObjectID id;
    private NumberOfElements numberOfElements;
    private DataArray traceData;

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                id.getBytes(),
                period.getBytes(),
                startDate.getBytes(),
                numberOfElements.getBytes(),
                traceData.getBytes()
        ));
    }

    @Override
    public TraceQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

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

        traceData = new DataArray(rawData.length - ptr).parse(rawData, ptr);

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

    public DataArray getTraceData() {
        return traceData;
    }

    public void setTraceData(DataArray traceData) {
        this.traceData = traceData;
    }
}