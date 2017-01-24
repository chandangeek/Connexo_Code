/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PointerTimeDateRegisterReadingBasePage extends AbstractBasePage {

    private int registerReadOffset;

    /** Creates a new instance of RealTimeBasePage */
    public PointerTimeDateRegisterReadingBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PointerTimeDateRegisterReadingBasePage:\n");
        strBuff.append("   registerReadOffset="+getRegisterReadOffset()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(312,2);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setRegisterReadOffset(ProtocolUtils.getInt(data,offset,2) - getBasePagesFactory().getMemStartAddress());
    }

    public int getRegisterReadOffset() {
        return registerReadOffset;
    }

    public void setRegisterReadOffset(int registerReadOffset) {
        this.registerReadOffset = registerReadOffset;
    }


} // public class RealTimeBasePage extends AbstractBasePage
