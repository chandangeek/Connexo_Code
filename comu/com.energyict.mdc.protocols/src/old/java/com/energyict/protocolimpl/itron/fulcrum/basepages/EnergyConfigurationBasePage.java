/*
 * EnergyConfigurationBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EnergyConfigurationBasePage extends AbstractBasePage {

    private int energyconfiguration;

    /** Creates a new instance of EnergyConfigurationBasePage */
    public EnergyConfigurationBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyConfigurationBasePage:\n");
        strBuff.append("   energyconfiguration="+getEnergyconfiguration()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x2812,1);
    }

    protected void parse(byte[] data) throws IOException {
        setEnergyconfiguration((int)data[0]&0xFF);
    }

    public int getEnergyconfiguration() {
        return energyconfiguration;
    }

    private void setEnergyconfiguration(int energyconfiguration) {
        this.energyconfiguration = energyconfiguration;
    }


} // public class RealTimeBasePage extends AbstractBasePage
