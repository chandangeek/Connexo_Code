package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

import java.io.UnsupportedEncodingException;

/**
 * Class for the VF (Manufacturer's software version) field in a CTR Structure Object - used in firmware upgrade process
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class VF extends AbstractField<VF> {

    private String vf;
    private static final int LENGTH = 6;

    public VF(byte[] vfBytes) {
        this.vf = ProtocolTools.getAsciiFromBytes(vfBytes);
    }

    public VF(String vfString) {
        this.vf = vfString;
    }

    public VF() {
    }

    public String getVF() {
        return vf;
    }

    public void setVF(String vf) {
        this.vf = vf;
    }

    public byte[] getBytes() {
        try {
            byte[] vfBytes = vf.getBytes("ASCII");
            if (vfBytes.length < 6) {
                return ProtocolTools.concatByteArrays(new byte[6 - vfBytes.length], vfBytes);
            } else {
                return ProtocolTools.getSubArray(vfBytes, 0, 6);
            }
        } catch (UnsupportedEncodingException e) {
            return new byte[6];
        }
    }

    public VF parse(byte[] rawData, int offset) throws CTRParsingException {
        setVF(ProtocolTools.getAsciiFromBytes(rawData));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }
}
