/*
 * DefaultViewIdConfiguration.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DefaultViewIdConfiguration extends AbstractDataDefinition {
    
    private int opticalPortView;
    private int serial1PortView;
    private int serial2PortView;
    
    /**
     * Creates a new instance of DefaultViewIdConfiguration
     */
    public DefaultViewIdConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DefaultViewIdConfiguration:\n");
        strBuff.append("   opticalPortView="+getOpticalPortView()+"\n");
        strBuff.append("   serial1PortView="+getSerial1PortView()+"\n");
        strBuff.append("   serial2PortView="+getSerial2PortView()+"\n");
        return strBuff.toString();
    }
    
    protected int getVariableName() {
        return 64;
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setOpticalPortView(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setSerial1PortView(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setSerial2PortView(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        
    }

    public int getOpticalPortView() {
        return opticalPortView;
    }

    public void setOpticalPortView(int opticalPortView) {
        this.opticalPortView = opticalPortView;
    }

    public int getSerial1PortView() {
        return serial1PortView;
    }

    public void setSerial1PortView(int serial1PortView) {
        this.serial1PortView = serial1PortView;
    }

    public int getSerial2PortView() {
        return serial2PortView;
    }

    public void setSerial2PortView(int serial2PortView) {
        this.serial2PortView = serial2PortView;
    }
}
