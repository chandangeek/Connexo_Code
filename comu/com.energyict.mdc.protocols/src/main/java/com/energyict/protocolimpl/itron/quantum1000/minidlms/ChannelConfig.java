/*
 * Result.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ChannelConfig {

    private QuantityId qid;
    private float ke;

    /** Creates a new instance of Result */
    public ChannelConfig(byte[] data,int offset) throws IOException {
        setQid(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setKe(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ChannelConfig:\n");
        strBuff.append("   ke="+getKe()+"\n");
        strBuff.append("   qid="+getQid()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 6;
    }

    public QuantityId getQid() {
        return qid;
    }

    public void setQid(QuantityId qid) {
        this.qid = qid;
    }

    public float getKe() {
        return ke;
    }

    public void setKe(float ke) {
        this.ke = ke;
    }



}
