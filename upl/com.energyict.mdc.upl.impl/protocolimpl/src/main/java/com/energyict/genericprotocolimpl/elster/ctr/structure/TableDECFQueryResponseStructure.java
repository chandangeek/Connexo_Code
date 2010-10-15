package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class TableDECFQueryResponseStructure extends Data<TableDECFQueryResponseStructure> {

    //See p64, TABLE DECF structure
    private CTRAbstractValue<String> pdr;
    private CTRAbstractValue<BigDecimal>[] dataAndOraS;
    private CTRAbstractValue<BigDecimal> diagnR;
    private CTRAbstractValue<BigDecimal> numberOfElements;
    private CTRAbstractValue<BigDecimal> id_Pt_Current;
    private CTRAbstractValue<BigDecimal> id_Pt_Previous;
    private AbstractCTRObject tot_Vb;
    private AbstractCTRObject tot_Vme;
    private AbstractCTRObject tot_Vme_f1;
    private AbstractCTRObject tot_Vme_f2;
    private AbstractCTRObject tot_Vme_f3;
    private AbstractCTRObject tot_Vcor_f1;
    private AbstractCTRObject tot_Vcor_f2;
    private AbstractCTRObject tot_Vcor_f3;
    private CTRAbstractValue<BigDecimal>[] dataAndOraP;
    private CTRAbstractValue diagnRS_pf;
    private AbstractCTRObject tot_Vb_pf;
    private AbstractCTRObject tot_Vme_pf;
    private AbstractCTRObject tot_Vme_pf_f1;
    private AbstractCTRObject tot_Vme_pf_f2;
    private AbstractCTRObject tot_Vme_pf_f3;
    private AbstractCTRObject tot_Vpre_f1;
    private AbstractCTRObject tot_Vpre_f2;
    private AbstractCTRObject tot_Vpre_f3;


    public TableDECFQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {

        byte[] valuesP = null;
        for (CTRAbstractValue<BigDecimal> value : dataAndOraP) {
            valuesP = ProtocolTools.concatByteArrays(valuesP, value.getBytes());
        }
        
        byte[] valuesS = null;
        for (CTRAbstractValue<BigDecimal> value : dataAndOraS) {
            valuesS = ProtocolTools.concatByteArrays(valuesS, value.getBytes());
        }

        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);
        type.setHasQualifier(true);
        type.setHasIdentifier(false);

        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                valuesS,
                diagnR.getBytes(),
                numberOfElements.getBytes(),
                id_Pt_Current.getBytes(),
                id_Pt_Previous.getBytes(),
                tot_Vb.getBytes(type),
                tot_Vme.getBytes(type),
                tot_Vme_f1.getBytes(type),
                tot_Vme_f2.getBytes(type),
                tot_Vme_f3.getBytes(type),
                tot_Vcor_f1.getBytes(type),
                tot_Vcor_f2.getBytes(type),
                tot_Vcor_f3.getBytes(type),
                valuesP,
                diagnRS_pf.getBytes(),
                tot_Vb_pf.getBytes(type),
                tot_Vme_pf.getBytes(type),
                tot_Vme_pf_f1.getBytes(type),
                tot_Vme_pf_f2.getBytes(type),
                tot_Vme_pf_f3.getBytes(type),
                tot_Vpre_f1.getBytes(type),
                tot_Vpre_f2.getBytes(type),
                tot_Vpre_f3.getBytes(type)
        ));
    }

    @Override
    public TableDECFQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);
        type.setHasQualifier(false);
        type.setHasIdentifier(false);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0").getValue()[0];
        ptr += pdr.getValueLength();

        dataAndOraS = factory.parse(rawData, ptr, type, "8.0.1").getValue();
        ptr += sum(dataAndOraS);

        diagnR = factory.parse(rawData, ptr, type, "12.2.0").getValue()[0];
        ptr += diagnR.getValueLength();

        numberOfElements = factory.parse(rawData, ptr, type, "10.1.0").getValue()[0];
        ptr += numberOfElements.getValueLength();

        id_Pt_Current = factory.parse(rawData, ptr, type, "17.0.4").getValue()[0];
        ptr += id_Pt_Current.getValueLength();

        id_Pt_Previous = factory.parse(rawData, ptr, type, "17.0.4").getValue()[1];
        ptr += id_Pt_Previous.getValueLength();
        type.setHasQualifier(true);        

        tot_Vb = factory.parse(rawData, ptr, type, "2.1.0");
        ptr += tot_Vb.getLength(type);

        tot_Vme = factory.parse(rawData, ptr, type, "2.3.0");
        ptr += tot_Vme.getLength(type);

        tot_Vme_f1 = factory.parse(rawData, ptr, type, "2.3.7");
        ptr += tot_Vme_f1.getLength(type);

        tot_Vme_f2 = factory.parse(rawData, ptr, type, "2.3.8");
        ptr += tot_Vme_f2.getLength(type);

        tot_Vme_f3 = factory.parse(rawData, ptr, type, "2.3.9");
        ptr += tot_Vme_f3.getLength(type);

        tot_Vcor_f1 = factory.parse(rawData, ptr, type, "2.5.0");
        ptr += tot_Vcor_f1.getLength(type);

        tot_Vcor_f2 = factory.parse(rawData, ptr, type, "2.5.1");
        ptr += tot_Vcor_f2.getLength(type);

        tot_Vcor_f3 = factory.parse(rawData, ptr, type, "2.5.2");
        ptr += tot_Vcor_f3.getLength(type);

        dataAndOraP = factory.parse(rawData, ptr, type, "8.0.2").getValue();
        ptr += sum(dataAndOraP);

        diagnRS_pf = factory.parse(rawData, ptr, type, "12.6.6").getValue()[0];
        ptr += diagnRS_pf.getValueLength();

        tot_Vb_pf = factory.parse(rawData, ptr, type, "2.1.6");
        ptr += tot_Vb_pf.getLength(type);

        tot_Vme_pf = factory.parse(rawData, ptr, type, "2.3.6");
        ptr += tot_Vme_pf.getLength(type);

        tot_Vme_pf_f1 = factory.parse(rawData, ptr, type, "2.3.A");
        ptr += tot_Vme_pf_f1.getLength(type);

        tot_Vme_pf_f2 = factory.parse(rawData, ptr, type, "2.3.B");
        ptr += tot_Vme_pf_f2.getLength(type);

        tot_Vme_pf_f3 = factory.parse(rawData, ptr, type, "2.3.C");
        ptr += tot_Vme_pf_f3.getLength(type);

        tot_Vpre_f1 = factory.parse(rawData, ptr, type, "2.5.3");
        ptr += tot_Vpre_f1.getLength(type);

        tot_Vpre_f2 = factory.parse(rawData, ptr, type, "2.5.4");
        ptr += tot_Vpre_f2.getLength(type);

        tot_Vpre_f3 = factory.parse(rawData, ptr, type, "2.5.5");
        ptr += tot_Vpre_f3.getLength(type);

        return this;
    }

    private int sum(CTRAbstractValue[] values) {
        int sum = 0;
        for (CTRAbstractValue value : values) {
            sum += value.getValueLength();
        }
        return sum;
    }
}