package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Puk_S;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ST;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.STCode;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 15:47:10
 */
public class IdentificationRequestStructure extends Data<IdentificationRequestStructure> {

    private Puk_S puks;
    private ST st;
    private STCode stCode;

    public IdentificationRequestStructure() {
        super(false);
        this.puks = new Puk_S();
        this.st = new ST();
        this.stCode = new STCode();
    }

    @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                puks.getBytes(),
                st.getBytes(),
                stCode.getBytes()
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     */
    @Override
    public IdentificationRequestStructure parse(byte[] rawData, int offset) {
        int ptr = offset;

        puks = new Puk_S().parse(rawData, ptr);
        ptr += puks.getLength();

        st = new ST().parse(rawData, ptr);
        ptr += st.getLength();

        stCode = new STCode().parse(rawData, ptr);
        ptr += stCode.getLength();

        return this;
    }

    public Puk_S getPuks() {
        return puks;
    }

    public void setPuks(Puk_S puks) {
        this.puks = puks;
    }

    public ST getSt() {
        return st;
    }

    public void setSt(ST st) {
        this.st = st;
    }

    public STCode getStCode() {
        return stCode;
    }

    public void setStCode(STCode stCode) {
        this.stCode = stCode;
    }
}
