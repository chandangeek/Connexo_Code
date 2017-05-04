/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * KYZDividersBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class KYZDividersBasePage extends AbstractBasePage {

    private int[] divider = new int[4];

    /** Creates a new instance of KYZDividersBasePage */
    public KYZDividersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("KYZDividersBasePage:\n");
        for (int i=0;i<getDivider().length;i++) {
            strBuff.append("       divider["+i+"]="+getDivider()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0xFE,0x2);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        getDivider()[0] = (data[0]&0xf)+1;
        getDivider()[1] = ((((int)data[0]&0xff) & 0xf0) >> 4)+1;
        getDivider()[2] = (data[1]&0xf)+1;
        getDivider()[3] = ((((int)data[1]&0xff) & 0xf0) >> 4)+1;

    }

    public int[] getDivider() {
        return divider;
    }

    public void setDivider(int[] divider) {
        this.divider = divider;
    }


} // public class RealTimeBasePage extends AbstractBasePage
