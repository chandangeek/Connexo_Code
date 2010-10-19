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
    private DateAndTimeCategory dataAndOraS;
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
    private DateAndTimeCategory dataAndOraP;
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

        AttributeType type = new AttributeType(0x00);
        type.setHasQualifier(true);
        type.setHasValueFields(true);

        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                dataAndOraS.getBytes(AttributeType.getValueOnly()),
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
                dataAndOraP.getBytes(AttributeType.getValueOnly()),
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
        type.setHasIdentifier(false);
        type.setHasValueFields(true);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0").getValue()[0];
        ptr += 7;

        dataAndOraS = (DateAndTimeCategory) factory.parse(rawData, ptr, type, "8.0.1");
        ptr += 5;

        diagnR = factory.parse(rawData, ptr, type, "12.2.0").getValue()[0];
        ptr += 2;

        numberOfElements = factory.parse(rawData, ptr, type, "10.1.0").getValue()[0];
        ptr += 2;

        id_Pt_Current = factory.parse(rawData, ptr, type, "17.0.4").getValue()[0];
        ptr += 2;

        id_Pt_Previous = factory.parse(rawData, ptr, type, "17.0.4").getValue()[1];
        ptr += 2;
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


        type.setHasQualifier(false);
        dataAndOraP = (DateAndTimeCategory) factory.parse(rawData, ptr, type, "8.0.2");
        ptr += dataAndOraP.getLength(AttributeType.getValueOnly());

        diagnRS_pf = factory.parse(rawData, ptr, type, "12.6.6").getValue()[0];
        ptr += diagnRS_pf.getValueLength();

        type.setHasQualifier(true);
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

    public DateAndTimeCategory getDataAndOraP() {
        return dataAndOraP;
    }

    public DateAndTimeCategory getDataAndOraS() {
        return dataAndOraS;
    }

    public CTRAbstractValue<BigDecimal> getDiagnR() {
        return diagnR;
    }

    public CTRAbstractValue getDiagnRS_pf() {
        return diagnRS_pf;
    }

    public CTRAbstractValue<BigDecimal> getId_Pt_Current() {
        return id_Pt_Current;
    }

    public CTRAbstractValue<BigDecimal> getId_Pt_Previous() {
        return id_Pt_Previous;
    }

    public CTRAbstractValue<BigDecimal> getNumberOfElements() {
        return numberOfElements;
    }

    public CTRAbstractValue<String> getPdr() {
        return pdr;
    }

    public AbstractCTRObject getTot_Vb() {
        return tot_Vb;
    }

    public AbstractCTRObject getTot_Vb_pf() {
        return tot_Vb_pf;
    }

    public AbstractCTRObject getTot_Vcor_f1() {
        return tot_Vcor_f1;
    }

    public AbstractCTRObject getTot_Vcor_f2() {
        return tot_Vcor_f2;
    }

    public AbstractCTRObject getTot_Vcor_f3() {
        return tot_Vcor_f3;
    }

    public AbstractCTRObject getTot_Vme() {
        return tot_Vme;
    }

    public AbstractCTRObject getTot_Vme_f1() {
        return tot_Vme_f1;
    }

    public AbstractCTRObject getTot_Vme_f2() {
        return tot_Vme_f2;
    }

    public AbstractCTRObject getTot_Vme_f3() {
        return tot_Vme_f3;
    }

    public AbstractCTRObject getTot_Vme_pf() {
        return tot_Vme_pf;
    }

    public AbstractCTRObject getTot_Vme_pf_f1() {
        return tot_Vme_pf_f1;
    }

    public AbstractCTRObject getTot_Vme_pf_f2() {
        return tot_Vme_pf_f2;
    }

    public AbstractCTRObject getTot_Vme_pf_f3() {
        return tot_Vme_pf_f3;
    }

    public AbstractCTRObject getTot_Vpre_f1() {
        return tot_Vpre_f1;
    }

    public AbstractCTRObject getTot_Vpre_f2() {
        return tot_Vpre_f2;
    }

    public AbstractCTRObject getTot_Vpre_f3() {
        return tot_Vpre_f3;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TableDECFQueryResponseStructure{").append('\n');
        sb.append("  dataAndOraP=").append(dataAndOraP).append('\n');
        sb.append("  pdr=").append(pdr).append('\n');
        sb.append("  dataAndOraS=").append(dataAndOraS).append('\n');
        sb.append("  diagnR=").append(diagnR).append('\n');
        sb.append("  numberOfElements=").append(numberOfElements).append('\n');
        sb.append("  id_Pt_Current=").append(id_Pt_Current).append('\n');
        sb.append("  id_Pt_Previous=").append(id_Pt_Previous).append('\n');
        sb.append("  tot_Vb=").append(tot_Vb).append('\n');
        sb.append("  tot_Vme=").append(tot_Vme).append('\n');
        sb.append("  tot_Vme_f1=").append(tot_Vme_f1).append('\n');
        sb.append("  tot_Vme_f2=").append(tot_Vme_f2).append('\n');
        sb.append("  tot_Vme_f3=").append(tot_Vme_f3).append('\n');
        sb.append("  tot_Vcor_f1=").append(tot_Vcor_f1).append('\n');
        sb.append("  tot_Vcor_f2=").append(tot_Vcor_f2).append('\n');
        sb.append("  tot_Vcor_f3=").append(tot_Vcor_f3).append('\n');
        sb.append("  diagnRS_pf=").append(diagnRS_pf).append('\n');
        sb.append("  tot_Vb_pf=").append(tot_Vb_pf).append('\n');
        sb.append("  tot_Vme_pf=").append(tot_Vme_pf).append('\n');
        sb.append("  tot_Vme_pf_f1=").append(tot_Vme_pf_f1).append('\n');
        sb.append("  tot_Vme_pf_f2=").append(tot_Vme_pf_f2).append('\n');
        sb.append("  tot_Vme_pf_f3=").append(tot_Vme_pf_f3).append('\n');
        sb.append("  tot_Vpre_f1=").append(tot_Vpre_f1).append('\n');
        sb.append("  tot_Vpre_f2=").append(tot_Vpre_f2).append('\n');
        sb.append("  tot_Vpre_f3=").append(tot_Vpre_f3).append('\n');
        sb.append('}');
        return sb.toString();
    }
}