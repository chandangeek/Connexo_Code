/*
 * ReadCommand.java
 *
 * Created on 21 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.cbo.*;
import java.io.*;


import com.energyict.protocol.*;
import com.energyict.protocolimpl.edmi.mk6.core.*;

/**
 *
 * @author koen
 */
public class ReadCommand extends AbstractCommand {
    
    private int registerId;
    private byte[] data;
    private AbstractRegisterType register;
    private Unit unit;
    
    /** Creates a new instance of ReadCommand */
    public ReadCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadCommand: ");
        strBuff.append("registerId=0x"+Integer.toHexString(getRegisterId())+", ");
        strBuff.append("data="+ProtocolUtils.outputHexString(getData())+", ");
        
        strBuff.append("register="+getRegister()+", ");
        strBuff.append("unit="+getUnit());
        return strBuff.toString();
    }    
    
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new ReadCommand(null)));
//    } 
    
    // Following the EDMI protocoldescription,R and M command should behave the same. Means all accepting 4 bytes as command.
    // However, only the M command seems to behave like that
    // Also, the EZiView software uses the M command
    private final char COMMAND='M'; // 'R'
    
    protected byte[] prepareBuild() {
       
       if (COMMAND=='M') { 
           byte[] data = new byte[5];
           data[0] = 'M';
           data[1] = (byte)((getRegisterId()>>24)&0xFF);
           data[2] = (byte)((getRegisterId()>>16)&0xFF);
           data[3] = (byte)((getRegisterId()>>8)&0xFF);
           data[4] = (byte)((getRegisterId())&0xFF);
           return data;       
       }
       else {
           byte[] data = new byte[3];
           data[0] = 'R';
           data[1] = (byte)((getRegisterId()>>8)&0xFF);
           data[2] = (byte)((getRegisterId())&0xFF);
           return data;         
       }
    }

    protected void parse(byte[] rawData) throws IOException {
        
        if (COMMAND != (char)rawData[0])
             throw new CommandResponseException("ReadCommand, request command "+COMMAND+" != response command "+(char)rawData[0]);
        
        if (COMMAND=='M') { 
            int tempRegisterId = ProtocolUtils.getInt(rawData,1,4);
            if (tempRegisterId != getRegisterId())
                throw new CommandResponseException("ReadCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
            setRegisterId(tempRegisterId);
            setData(ProtocolUtils.getSubArray(rawData,5, rawData.length-1));
        }
        else if (COMMAND=='R') {
            int tempRegisterId = ProtocolUtils.getInt(rawData,1,2);
            if (tempRegisterId != getRegisterId())
                throw new CommandResponseException("ReadCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
            setRegisterId(tempRegisterId);
            setData(ProtocolUtils.getSubArray(rawData,3, rawData.length-1));
        }
        
        
        InformationCommand ic = getCommandFactory().getInformationCommand(getRegisterId());
        RegisterTypeParser rtp = new RegisterTypeParser(getCommandFactory().getMk6().getTimeZone());
        register = rtp.parse2External(ic.getDataType(), getData());
        RegisterUnitParser rup = new RegisterUnitParser();
        setUnit(rup.parse(ic.getMeasurementUnit()));
        
    }
    
    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public AbstractRegisterType getRegister() {
        return register;
    }

    public void setRegister(AbstractRegisterType register) {
        this.register = register;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    
}
