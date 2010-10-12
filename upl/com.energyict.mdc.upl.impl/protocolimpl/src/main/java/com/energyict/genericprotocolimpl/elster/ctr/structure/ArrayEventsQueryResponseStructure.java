package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectFactory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.DataArray;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.Index_Q;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class ArrayEventsQueryResponseStructure extends Data<ArrayEventsQueryResponseStructure> {

    private CTRAbstractValue<String> pdr;
    private Qualifier t_Antif_qlf;
    private CTRAbstractValue<BigDecimal> t_Antif_Value;
    private Index_Q index_A;
    private CTRAbstractValue<BigDecimal> numberOfEvents;
    private DataArray evento_Short_1;
    private DataArray evento_Short_2;
    private DataArray evento_Short_3;


    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                t_Antif_qlf.getBytes(),
                t_Antif_Value.getBytes(),
                index_A.getBytes(),
                numberOfEvents.getBytes(),
                evento_Short_1.getBytes(),
                evento_Short_2.getBytes(),
                evento_Short_3.getBytes()
        ));
    }

    @Override
    public ArrayEventsQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType valueAttributeType = new AttributeType();
        valueAttributeType.setHasValueFields(true);

        pdr = factory.parse(rawData, ptr, valueAttributeType, "C.0.0").getValue()[0];
        ptr += pdr.getValueLength();

        valueAttributeType.setHasQualifier(true);
        t_Antif_qlf = factory.parse(rawData, ptr, valueAttributeType, "D.A.0").getQlf();
        t_Antif_Value = factory.parse(rawData, ptr, valueAttributeType, "D.A.0").getValue()[0];
        ptr += t_Antif_qlf.LENGTH;
        ptr += t_Antif_Value.getValueLength();

        index_A = new Index_Q().parse(rawData, ptr);
        ptr += Index_Q.LENGTH;

        valueAttributeType.setHasQualifier(false);
        numberOfEvents = factory.parse(rawData, ptr, valueAttributeType, "10.1.0").getValue()[0];
        ptr += numberOfEvents.getValueLength();

        evento_Short_1 = new DataArray(17).parse(rawData, ptr);
        ptr += evento_Short_1.getArrayLength();

        evento_Short_2 = new DataArray(68).parse(rawData, ptr);
        ptr += evento_Short_2.getArrayLength();

        evento_Short_3 = new DataArray(17).parse(rawData, ptr);
        ptr += evento_Short_3.getArrayLength();

        return this;
    }


    public CTRAbstractValue<String> getPdr() {
        return pdr;
    }

    public void setPdr(CTRAbstractValue<String> pdr) {
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

    public DataArray getEvento_Short_1() {
        return evento_Short_1;
    }

    public void setEvento_Short_1(DataArray evento_Short_1) {
        this.evento_Short_1 = evento_Short_1;
    }

    public DataArray getEvento_Short_2() {
        return evento_Short_2;
    }

    public void setEvento_Short_2(DataArray evento_Short_2) {
        this.evento_Short_2 = evento_Short_2;
    }

    public DataArray getEvento_Short_3() {
        return evento_Short_3;
    }

    public void setEvento_Short_3(DataArray evento_Short_3) {
        this.evento_Short_3 = evento_Short_3;
    }
}