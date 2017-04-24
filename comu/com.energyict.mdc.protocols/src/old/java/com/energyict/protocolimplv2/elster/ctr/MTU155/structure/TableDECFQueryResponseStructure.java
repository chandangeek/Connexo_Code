/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TableDECFQueryResponseStructure extends AbstractTableQueryResponseStructure<TableDECFQueryResponseStructure> {

    private static final List<String> CAPTURED_OBJECTS;

    static {
        CAPTURED_OBJECTS = new ArrayList();
        CAPTURED_OBJECTS.add("C.0.0");
        CAPTURED_OBJECTS.add("8.0.1");
        CAPTURED_OBJECTS.add("12.2.0");
        CAPTURED_OBJECTS.add("10.1.0");
        CAPTURED_OBJECTS.add("2.1.0");
        CAPTURED_OBJECTS.add("2.3.0");
        CAPTURED_OBJECTS.add("2.3.7");
        CAPTURED_OBJECTS.add("2.3.8");
        CAPTURED_OBJECTS.add("2.3.9");
        CAPTURED_OBJECTS.add("2.5.0");
        CAPTURED_OBJECTS.add("2.5.1");
        CAPTURED_OBJECTS.add("2.5.2");
        CAPTURED_OBJECTS.add("8.0.2");
        CAPTURED_OBJECTS.add("12.6.6");
        CAPTURED_OBJECTS.add("2.1.6");
        CAPTURED_OBJECTS.add("2.3.6");
        CAPTURED_OBJECTS.add("2.3.A");
        CAPTURED_OBJECTS.add("2.3.B");
        CAPTURED_OBJECTS.add("2.3.C");
        CAPTURED_OBJECTS.add("2.5.3");
        CAPTURED_OBJECTS.add("2.5.4");
        CAPTURED_OBJECTS.add("2.5.5");
    }

    //See documentation p. 64, TABLE DECF structure
    private AbstractCTRObject pdr;
    private AbstractCTRObject dataAndOraS;
    private AbstractCTRObject diagnR;
    private AbstractCTRObject numberOfElements;
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
    private AbstractCTRObject dataAndOraP;
    private AbstractCTRObject diagnRS_pf;
    private AbstractCTRObject tot_Vb_pf;
    private AbstractCTRObject tot_Vme_pf;
    private AbstractCTRObject tot_Vme_pf_f1;
    private AbstractCTRObject tot_Vme_pf_f2;
    private AbstractCTRObject tot_Vme_pf_f3;
    private AbstractCTRObject tot_Vpre_f1;
    private AbstractCTRObject tot_Vpre_f2;
    private AbstractCTRObject tot_Vpre_f3;

    /**
     * @return a list of all objects in this table
     */
    public List<AbstractCTRObject> getObjects() {
        List<AbstractCTRObject> list = new ArrayList();
        list.add(pdr);
        list.add(dataAndOraS);
        list.add(diagnR);
        list.add(numberOfElements);
        list.add(tot_Vb);
        list.add(tot_Vme);
        list.add(tot_Vme_f1);
        list.add(tot_Vme_f2);
        list.add(tot_Vme_f3);
        list.add(tot_Vcor_f1);
        list.add(tot_Vcor_f2);
        list.add(tot_Vcor_f3);
        list.add(dataAndOraP);
        list.add(diagnRS_pf);
        list.add(tot_Vb_pf);
        list.add(tot_Vme_pf);
        list.add(tot_Vme_pf_f1);
        list.add(tot_Vme_pf_f2);
        list.add(tot_Vme_pf_f3);
        list.add(tot_Vpre_f1);
        list.add(tot_Vpre_f2);
        list.add(tot_Vpre_f3);

        return list;
    }

    public TableDECFQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    /**
     * Check if the DECF table contains an object with the given ID
     *
     * @param id
     * @return true or false
     */
    public static boolean containsObjectId(CTRObjectID id) {
        for (String capturedId : CAPTURED_OBJECTS) {
            if (id.is(capturedId)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public byte[] getBytes() {

        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                dataAndOraS.getBytes(),
                diagnR.getBytes(),
                numberOfElements.getBytes(),
                id_Pt_Current.getBytes(),
                id_Pt_Previous.getBytes(),
                tot_Vb.getBytes(),
                tot_Vme.getBytes(),
                tot_Vme_f1.getBytes(),
                tot_Vme_f2.getBytes(),
                tot_Vme_f3.getBytes(),
                tot_Vcor_f1.getBytes(),
                tot_Vcor_f2.getBytes(),
                tot_Vcor_f3.getBytes(),
                dataAndOraP.getBytes(),
                diagnRS_pf.getBytes(),
                tot_Vb_pf.getBytes(),
                tot_Vme_pf.getBytes(),
                tot_Vme_pf_f1.getBytes(),
                tot_Vme_pf_f2.getBytes(),
                tot_Vme_pf_f3.getBytes(),
                tot_Vpre_f1.getBytes(),
                tot_Vpre_f2.getBytes(),
                tot_Vpre_f3.getBytes()
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
    public TableDECFQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasIdentifier(false);
        type.setHasValueFields(true);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0");
        ptr += pdr.getLength();

        dataAndOraS = factory.parse(rawData, ptr, type, "8.0.1");
        ptr += dataAndOraS.getLength();

        diagnR = factory.parse(rawData, ptr, type, "12.2.0");
        ptr += diagnR.getLength();

        numberOfElements = factory.parse(rawData, ptr, type, "10.1.0");
        ptr += numberOfElements.getLength();

        id_Pt_Current = factory.parse(rawData, ptr, type, "17.0.4").getValue()[0];
        ptr += id_Pt_Current.getValueLength();

        id_Pt_Previous = factory.parse(rawData, ptr, type, "17.0.4").getValue()[1];
        ptr += id_Pt_Previous.getValueLength();
        type.setHasQualifier(true);

        tot_Vb = factory.parse(rawData, ptr, type, "2.1.0");
        ptr += tot_Vb.getLength();

        tot_Vme = factory.parse(rawData, ptr, type, "2.3.0");
        ptr += tot_Vme.getLength();

        tot_Vme_f1 = factory.parse(rawData, ptr, type, "2.3.7");
        ptr += tot_Vme_f1.getLength();

        tot_Vme_f2 = factory.parse(rawData, ptr, type, "2.3.8");
        ptr += tot_Vme_f2.getLength();

        tot_Vme_f3 = factory.parse(rawData, ptr, type, "2.3.9");
        ptr += tot_Vme_f3.getLength();

        tot_Vcor_f1 = factory.parse(rawData, ptr, type, "2.5.0");
        ptr += tot_Vcor_f1.getLength();

        tot_Vcor_f2 = factory.parse(rawData, ptr, type, "2.5.1");
        ptr += tot_Vcor_f2.getLength();

        tot_Vcor_f3 = factory.parse(rawData, ptr, type, "2.5.2");
        ptr += tot_Vcor_f3.getLength();

        type.setHasQualifier(false);

        dataAndOraP = factory.parse(rawData, ptr, type, "8.0.2");
        ptr += dataAndOraP.getLength();

        diagnRS_pf = factory.parse(rawData, ptr, type, "12.6.6");
        ptr += diagnRS_pf.getLength();

        type.setHasQualifier(true);

        tot_Vb_pf = factory.parse(rawData, ptr, type, "2.1.6");
        ptr += tot_Vb_pf.getLength();

        tot_Vme_pf = factory.parse(rawData, ptr, type, "2.3.6");
        ptr += tot_Vme_pf.getLength();

        tot_Vme_pf_f1 = factory.parse(rawData, ptr, type, "2.3.A");
        ptr += tot_Vme_pf_f1.getLength();

        tot_Vme_pf_f2 = factory.parse(rawData, ptr, type, "2.3.B");
        ptr += tot_Vme_pf_f2.getLength();

        tot_Vme_pf_f3 = factory.parse(rawData, ptr, type, "2.3.C");
        ptr += tot_Vme_pf_f3.getLength();

        tot_Vpre_f1 = factory.parse(rawData, ptr, type, "2.5.3");
        ptr += tot_Vpre_f1.getLength();

        tot_Vpre_f2 = factory.parse(rawData, ptr, type, "2.5.4");
        ptr += tot_Vpre_f2.getLength();

        tot_Vpre_f3 = factory.parse(rawData, ptr, type, "2.5.5");
        ptr += tot_Vpre_f3.getLength();

        return this;
    }

    public AbstractCTRObject getDataAndOraP() {
        return dataAndOraP;
    }

    public AbstractCTRObject getDataAndOraS() {
        return dataAndOraS;
    }

    public AbstractCTRObject getDiagnR() {
        return diagnR;
    }

    public AbstractCTRObject getDiagnRS_pf() {
        return diagnRS_pf;
    }

    public CTRAbstractValue<BigDecimal> getId_Pt_Current() {
        return id_Pt_Current;
    }

    public CTRAbstractValue<BigDecimal> getId_Pt_Previous() {
        return id_Pt_Previous;
    }

    public AbstractCTRObject getNumberOfElements() {
        return numberOfElements;
    }

    public AbstractCTRObject getPdr() {
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