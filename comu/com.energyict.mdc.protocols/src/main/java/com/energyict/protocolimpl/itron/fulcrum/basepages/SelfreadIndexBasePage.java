/*
 * SelfreadIndexBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfreadIndexBasePage extends AbstractBasePage {

    private int index;

    /** Creates a new instance of SelfreadIndexBasePage */
    public SelfreadIndexBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfreadIndexBasePage:\n");
        strBuff.append("   index="+getIndex()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x33B5,0x1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setIndex(ProtocolUtils.getInt(data,0,1));
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


} // public class RealTimeBasePage extends AbstractBasePage
