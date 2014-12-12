package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.EventCategory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayEventsQueryResponseStructure extends Data<ArrayEventsQueryResponseStructure> {

    public static final int NUMBER_OF_EVENT_RECORDS = 6;
    private AbstractCTRObject pdr;
    private Qualifier t_Antif_qlf;
    private CTRAbstractValue<BigDecimal> t_Antif_Value;
    private Index_Q index_A;
    private CTRAbstractValue<BigDecimal> numberOfEvents;
    private CTRAbstractValue[][] evento_Short = new CTRAbstractValue[NUMBER_OF_EVENT_RECORDS][];

    public ArrayEventsQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {

        byte[] valueBytes = null;
        for (CTRAbstractValue[] valueArray : evento_Short) {
            for (CTRAbstractValue value : valueArray) {
                valueBytes = ProtocolTools.concatByteArrays(valueBytes, value.getBytes());
            }
        }

        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                t_Antif_qlf.getBytes(),
                t_Antif_Value.getBytes(),
                index_A.getBytes(),
                numberOfEvents.getBytes(),
                valueBytes
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
    public ArrayEventsQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType valueAttributeType = new AttributeType();
        valueAttributeType.setHasValueFields(true);

        pdr = factory.parse(rawData, ptr, valueAttributeType, "C.0.0");
        ptr += pdr.getLength();

        valueAttributeType.setHasQualifier(true);
        t_Antif_qlf = factory.parse(rawData, ptr, valueAttributeType, "D.A.0").getQlf();
        t_Antif_Value = factory.parse(rawData, ptr, valueAttributeType, "D.A.0").getValue()[0];
        ptr += t_Antif_qlf.getLength();
        ptr += t_Antif_Value.getValueLength();

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += index_A.getLength();

        valueAttributeType.setHasQualifier(false);
        numberOfEvents = factory.parse(rawData, ptr, valueAttributeType, "10.1.0").getValue()[0];
        ptr += numberOfEvents.getValueLength();

        //Parse the 6 received event records
        for (int i = 0; i < NUMBER_OF_EVENT_RECORDS; i++) {
            evento_Short[i] = factory.parse(rawData, ptr, valueAttributeType, "10.0.1").getValue();
            ptr += EventCategory.EVENT_LENGTH;
        }

        return this;
    }

    public AbstractCTRObject getPdr() {
        return pdr;
    }

    public void setPdr(AbstractCTRObject pdr) {
        this.pdr = pdr;
    }

    public Qualifier getT_Antif_qlf() {
        return t_Antif_qlf;
    }

    public void setT_Antif_qlf(Qualifier t_Antif_qlf) {
        this.t_Antif_qlf = t_Antif_qlf;
    }

    public CTRAbstractValue<BigDecimal> getT_Antif_Value() {
        return t_Antif_Value;
    }

    public void setT_Antif_Value(CTRAbstractValue<BigDecimal> t_Antif_Value) {
        this.t_Antif_Value = t_Antif_Value;
    }

    public Index_Q getIndex_A() {
        return index_A;
    }

    public void setIndex_A(Index_Q index_A) {
        this.index_A = index_A;
    }

    public CTRAbstractValue<BigDecimal> getNumberOfEvents() {
        return numberOfEvents;
    }

    public void setNumberOfEvents(CTRAbstractValue<BigDecimal> numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public CTRAbstractValue[][] getEvento_Short() {
        return evento_Short;
    }

    public void setEvento_Short(CTRAbstractValue[][] evento_Short) {
        this.evento_Short = evento_Short;
    }
}