/*
 * FirmwareAndSoftwareRevision.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class FirmwareAndSoftwareRevision extends AbstractBasePage {


    private int fwVersion;

    /** Creates a new instance of FirmwareAndSoftwareRevision */
    public FirmwareAndSoftwareRevision(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FirmwareAndSoftwareRevision:\n");
        strBuff.append("   fwVersion="+getFwVersion()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0xA5,1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setFwVersion(ProtocolUtils.getInt(data,offset,1));
    }

    public int getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(int fwVersion) {
        this.fwVersion = fwVersion;
    }





} // public class RealTimeBasePage extends AbstractBasePage
