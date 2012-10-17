package test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.common.AttributeType;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRParsingException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.frame.field.Data;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.object.CTRObjectFactory;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.object.field.CTRObjectID;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure.field.ReferenceDate;

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

    public Trace_CQueryRequestStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pssw.getBytes(),
                id.getBytes(),
                period.getBytes(),
                date.getBytes()
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
    public Trace_CQueryRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        pssw = factory.parse(rawData, ptr, type, "D.0.1").getValue()[0];
        ptr += pssw.getValueLength();

        id = new CTRObjectID().parse(rawData, ptr);
        ptr += id.getLength();

        period = new PeriodTrace_C().parse(rawData, ptr);
        ptr += period.getLength();

        date = new ReferenceDate().parse(rawData, ptr);
        ptr += date.getLength();
        
        return this;
    }

    public CTRAbstractValue<String> getPssw() {
        return pssw;
    }

    public void setPssw(CTRAbstractValue<String> pssw) {
        this.pssw = pssw;
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
}