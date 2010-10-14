package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

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

    @Override
    public IdentificationRequestStructure parse(byte[] rawData, int offset) {
        int ptr = offset;

        puks = new Puk_S().parse(rawData, ptr);
        ptr += Puk_S.LENGTH;

        st = new ST().parse(rawData, ptr);
        ptr += ST.LENGTH;

        stCode = new STCode().parse(rawData, ptr);
        ptr += STCode.LENGTH;

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
