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
public class TraceQueryRequestStructure extends Data<TraceQueryRequestStructure> {

    private CTRAbstractValue<String> pssw;
    private PeriodTrace period;
    private StartDate startDate;
    private CTRObjectID id;
    private NumberOfElements numberOfElements;

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                id.getBytes(),
                period.getBytes(),
                startDate.getBytes(),
                numberOfElements.getBytes()
        ));
    }

    @Override
    public TraceQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        byte[] b = ProtocolTools.getSubArray(rawData, ptr, ptr + CTRObjectID.LENGTH);
        id = new CTRObjectID().parse(b, 0);
        ptr += CTRObjectID.LENGTH;

        period = new PeriodTrace().parse(rawData, ptr);
        ptr += PeriodTrace.LENGTH;

        startDate = new StartDate().parse(rawData, ptr);
        ptr += StartDate.LENGTH;

        numberOfElements = new NumberOfElements().parse(rawData, ptr);
        ptr += NumberOfElements.LENGTH;

        return this;
    }

    public CTRAbstractValue<String> getPssw() {
        return pssw;
    }

    public void setPssw(CTRAbstractValue<String> pssw) {
        this.pssw = pssw;
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
}