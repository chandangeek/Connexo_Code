package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.IdentificationProcessIdentify;

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
        CAPTURED_OBJECTS.add("12.0.0");
        CAPTURED_OBJECTS.add("D.6.3");
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
    private AbstractCTRObject sd;
    private AbstractCTRObject pukS;
    private AbstractCTRObject idPT;
    private IdentificationProcessIdentify identify;

    public IdentificationResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    /**
     * Check if the DECF table contains an object with the given ID
     *
     * @param id: the given id
     * @return boolean, whether or not the table contains the given ID
     */
    public static boolean containsObjectId(CTRObjectID id) {
        for (String capturedId : CAPTURED_OBJECTS) {
            if (id.is(capturedId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a list of objects
     * @return list of objects
     */
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
        objects.add(sd);
        objects.add(pukS);
        return objects;
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes()
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

        this.sd = factory.parse(rawData, ptr, valueAttributeType, "12.0.0");
        ptr += sd.getLength();

        this.pukS = factory.parse(rawData, ptr, valueAttributeType, "D.6.3");
        ptr += pukS.getLength();

        this.idPT = factory.parse(rawData, ptr, valueAttributeType, "17.0.4");
        ptr += this.idPT.getLength();

        ptr += 11;  // ID-SFTW
        ptr += 10;  // Reserved field

        this.identify = new IdentificationProcessIdentify().parse(rawData, ptr);
        ptr += identify.getLength();

        return this;
    }

    public CTRAbstractValue<String> getPdr() {
        return pdr.getValue(0);
    }

    /**
     * This is the serial number of the GAS meter
     * @return
     */
    public String getMeterSerialNumber() {
        return getAnCont().getValue(2).getStringValue().trim();
    }

    public AbstractCTRObject getCcode() {
        return ccode;
    }

    public AbstractCTRObject getCia() {
        return cia;
    }

    public AbstractCTRObject getCca() {
        return cca;
    }

    public AbstractCTRObject getVf() {
        return vf;
    }

    public AbstractCTRObject getCap() {
        return cap;
    }

    public AbstractCTRObject getVsPro() {
        return vsPro;
    }

    public AbstractCTRObject getSyncT() {
        return syncT;
    }

    public AbstractCTRObject getNcg() {
        return ncg;
    }

    public AbstractCTRObject getEmSize() {
        return emSize;
    }

    public AbstractCTRObject getAnCont() {
        return anCont;
    }

    public AbstractCTRObject getNem() {
        return nem;
    }

    public AbstractCTRObject getNea() {
        return nea;
    }

    public AbstractCTRObject getNet() {
        return net;
    }

    public AbstractCTRObject getPukS() {
        return pukS;
    }

    public AbstractCTRObject getSd() {
        return sd;
    }

    public AbstractCTRObject getIdPT() {
        return idPT;
    }

    public IdentificationProcessIdentify getIdentify() {
        return identify;
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
        sb.append(", sd=").append(sd).append('\n');
        sb.append(", pukS=").append(pukS).append('\n');
        sb.append('}');
        return sb.toString();
    }
}
