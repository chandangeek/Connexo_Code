package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class IdentificationResponseStructure extends AbstractTableQueryResponseStructure<IdentificationResponseStructure> {

    private static final List<String> CAPTURED_OBJECTS;

    static {
        CAPTURED_OBJECTS = new ArrayList();
        CAPTURED_OBJECTS.add("C.0.0");
        CAPTURED_OBJECTS.add("9.0.1");
        CAPTURED_OBJECTS.add("9.0.2");
        CAPTURED_OBJECTS.add("9.0.3");
        CAPTURED_OBJECTS.add("9.0.4");
        CAPTURED_OBJECTS.add("9.0.5");
        CAPTURED_OBJECTS.add("9.0.7");
        CAPTURED_OBJECTS.add("9.0.9");
        CAPTURED_OBJECTS.add("C.0.1");
        CAPTURED_OBJECTS.add("9.1.1");
        CAPTURED_OBJECTS.add("C.2.0");
        CAPTURED_OBJECTS.add("10.1.0");
        CAPTURED_OBJECTS.add("10.2.0");
        CAPTURED_OBJECTS.add("10.3.0");
        CAPTURED_OBJECTS.add("D.6.3");
        CAPTURED_OBJECTS.add("12.0.0");
    }

    private AbstractCTRObject pdr;
    private AbstractCTRObject ccode;
    private AbstractCTRObject cia;
    private AbstractCTRObject cca;
    private AbstractCTRObject vf;
    private AbstractCTRObject cap;
    private AbstractCTRObject vsPro;
    private AbstractCTRObject syncT;
    private AbstractCTRObject ncg;
    private AbstractCTRObject emSize;
    private AbstractCTRObject anCont;
    private AbstractCTRObject nem;
    private AbstractCTRObject nea;
    private AbstractCTRObject net;
    private AbstractCTRObject pukS;
    private AbstractCTRObject sd;

    public IdentificationResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    /**
     * Check if the DECF table contains an object with the given ID
     *
     * @param id
     * @return
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
    public List<AbstractCTRObject> getObjects() {
        List<AbstractCTRObject> objects = new ArrayList<AbstractCTRObject>();
        objects.add(pdr);
        objects.add(ccode);
        objects.add(cia);
        objects.add(cca);
        objects.add(vf);
        objects.add(cap);
        objects.add(vsPro);
        objects.add(syncT);
        objects.add(ncg);
        objects.add(emSize);
        objects.add(anCont);
        objects.add(nem);
        objects.add(nea);
        objects.add(net);
        objects.add(pukS);
        objects.add(sd);
        return objects;
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes()
        ));
    }

    @Override
    public IdentificationResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType valueAttributeType = new AttributeType();
        valueAttributeType.setHasValueFields(true);

        this.pdr = factory.parse(rawData, ptr, valueAttributeType, "C.0.0");
        ptr += pdr.getLength();

        this.ccode = factory.parse(rawData, ptr, valueAttributeType, "9.0.1");
        ptr += ccode.getLength();

        this.cia = factory.parse(rawData, ptr, valueAttributeType, "9.0.2");
        ptr += cia.getLength();

        this.cca = factory.parse(rawData, ptr, valueAttributeType, "9.0.3");
        ptr += cca.getLength();

        this.vf = factory.parse(rawData, ptr, valueAttributeType, "9.0.4");
        ptr += vf.getLength();

        this.cap = factory.parse(rawData, ptr, valueAttributeType, "9.0.5");
        ptr += cap.getLength();

        this.vsPro = factory.parse(rawData, ptr, valueAttributeType, "9.0.7");
        ptr += vsPro.getLength();

        this.syncT = factory.parse(rawData, ptr, valueAttributeType, "9.0.9");
        ptr += syncT.getLength();

        this.ncg = factory.parse(rawData, ptr, valueAttributeType, "C.0.1");
        ptr += ncg.getLength();

        this.emSize = factory.parse(rawData, ptr, valueAttributeType, "9.1.1");
        ptr += emSize.getLength();

        this.anCont = factory.parse(rawData, ptr, valueAttributeType, "C.2.0");
        ptr += anCont.getLength();

        this.nem = factory.parse(rawData, ptr, valueAttributeType, "10.1.0");
        ptr += nem.getLength();

        this.nea = factory.parse(rawData, ptr, valueAttributeType, "10.2.0");
        ptr += nea.getLength();

        this.net = factory.parse(rawData, ptr, valueAttributeType, "10.3.0");
        ptr += net.getLength();

        this.pukS = factory.parse(rawData, ptr, valueAttributeType, "D.6.3");
        ptr += pukS.getLength();

        this.sd = factory.parse(rawData, ptr, valueAttributeType, "12.0.0");
        ptr += sd.getLength();

        return this;
    }

    public CTRAbstractValue<String> getPdr() {
        return pdr.getValue(0);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IdentificationResponseStructure");
        sb.append("{pdr=").append(pdr).append('\n');
        sb.append(", ccode=").append(ccode).append('\n');
        sb.append(", cia=").append(cia).append('\n');
        sb.append(", cca=").append(cca).append('\n');
        sb.append(", vf=").append(vf).append('\n');
        sb.append(", cap=").append(cap).append('\n');
        sb.append(", vsPro=").append(vsPro).append('\n');
        sb.append(", syncT=").append(syncT).append('\n');
        sb.append(", ncg=").append(ncg).append('\n');
        sb.append(", emSize=").append(emSize).append('\n');
        sb.append(", anCont=").append(anCont).append('\n');
        sb.append(", nem=").append(nem).append('\n');
        sb.append(", nea=").append(nea).append('\n');
        sb.append(", net=").append(net).append('\n');
        sb.append(", pukS=").append(pukS).append('\n');
        sb.append(", sd=").append(sd).append('\n');
        sb.append('}');
        return sb.toString();
    }
}
