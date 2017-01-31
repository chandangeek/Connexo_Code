/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ModelTypeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ModelTypeBasePage extends AbstractBasePage {

    private boolean demandOnly;
    private boolean demandTOU;
    private boolean demandTOUMassMemory;


    /** Creates a new instance of ModelTypeBasePage */
    public ModelTypeBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ModelTypeBasePage:\n");
        strBuff.append("   demandOnly="+isDemandOnly()+"\n");
        strBuff.append("   demandTOU="+isDemandTOU()+"\n");
        strBuff.append("   demandTOUMassMemory="+isDemandTOUMassMemory()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2110,1);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        int temp = (int)data[0]&0xFF;
        setDemandOnly((temp & 0x01) == 0x01);
        setDemandTOU((temp & 0x02) == 0x02);
        setDemandTOUMassMemory((temp & 0x04) == 0x04);
    }

    public boolean isDemandOnly() {
        return demandOnly;
    }

    public void setDemandOnly(boolean demandOnly) {
        this.demandOnly = demandOnly;
    }

    public boolean isDemandTOU() {
        return demandTOU;
    }

    public void setDemandTOU(boolean demandTOU) {
        this.demandTOU = demandTOU;
    }

    public boolean isDemandTOUMassMemory() {
        return demandTOUMassMemory;
    }

    public void setDemandTOUMassMemory(boolean demandTOUMassMemory) {
        this.demandTOUMassMemory = demandTOUMassMemory;
    }



} // public class FirmwareOptionsBasePage extends AbstractBasePage
