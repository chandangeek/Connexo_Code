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

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class FirmwareRevisionBasePage extends AbstractBasePage {

    private int firmwareRevision;

    /** Creates a new instance of RealTimeBasePage */
    public FirmwareRevisionBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FirmwareRevisionBasePage:\n");
        strBuff.append("   firmwareRevision="+getFirmwareRevision()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x0,1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setFirmwareRevision((int)data[0] & 0xFF);
    }

    public int getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(int firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }


} // public class RealTimeBasePage extends AbstractBasePage
