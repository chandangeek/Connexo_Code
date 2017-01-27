/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SerialNumberCommand extends AbstractCommand {
    
    private long serialNumber;
    private String dspRevision;        
    
    /** Creates a new instance of TemplateCommand */
    public SerialNumberCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SerialNumberCommand:\n");
        strBuff.append("   serialNumber="+getSerialNumber()+"\n");
        strBuff.append("   dspRevision="+getDspRevision()+"\n");        
        return strBuff.toString();
    } 
    
    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x87,0,0,0,0,0,0,0,0};
    }
    
    protected void parse(byte[] data) throws IOException {
        setSerialNumber(getSerialNumber() + ProtocolUtils.BCD2hex(data[0]));
        setSerialNumber(getSerialNumber() + (ProtocolUtils.BCD2hex(data[1])*100));
        setSerialNumber(getSerialNumber() + (ProtocolUtils.BCD2hex(data[2])*10000));
        setSerialNumber(getSerialNumber() + (ProtocolUtils.BCD2hex(data[3])*1000000));
        
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            setDspRevision(""+ProtocolUtils.BCD2hex(data[4])+"."+ProtocolUtils.BCD2hex(data[5]));
        }
        if (getCommandFactory().getFirmwareVersionCommand().isDX()) {
            setDspRevision("UNKNOWN (DX meter)");
        }
        
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    private void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDspRevision() {
        return dspRevision;
    }

    private void setDspRevision(String dspRevision) {
        this.dspRevision = dspRevision;
    }
}
