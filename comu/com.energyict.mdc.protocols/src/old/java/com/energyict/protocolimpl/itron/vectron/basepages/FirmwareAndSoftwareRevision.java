/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FirmwareAndSoftwareRevision.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class FirmwareAndSoftwareRevision extends AbstractBasePage {


    private BigDecimal swVersion;
    private BigDecimal fwVersion;

    /** Creates a new instance of FirmwareAndSoftwareRevision */
    public FirmwareAndSoftwareRevision(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FirmwareAndSoftwareRevision:\n");
        strBuff.append("   swVersion="+getSwVersion()+"\n");
        strBuff.append("   fwVersion="+getFwVersion()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2201,4);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setSwVersion(ParseUtils.convertBCDFixedPoint(data, offset, 2, 8));
        offset+=2;
        setFwVersion(ParseUtils.convertBCDFixedPoint(data, offset, 2, 8));
    }

    public BigDecimal getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(BigDecimal swVersion) {
        this.swVersion = swVersion;
    }

    public BigDecimal getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(BigDecimal fwVersion) {
        this.fwVersion = fwVersion;
    }



} // public class RealTimeBasePage extends AbstractBasePage
