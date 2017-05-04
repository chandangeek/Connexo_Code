/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
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

/**
 *
 * @author Koen
 */
public class FrontEndFirmwareVersionBasePage extends AbstractBasePage {


    private int version;

    /** Creates a new instance of RealTimeBasePage */
    public FrontEndFirmwareVersionBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FrontEndFirmwareVersionBasePage:\n");
        strBuff.append("   version="+getVersion()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x1b05,2);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setVersion((int)ParseUtils.getBCD2Long(data, offset, 2));
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


} // public class RealTimeBasePage extends AbstractBasePage
