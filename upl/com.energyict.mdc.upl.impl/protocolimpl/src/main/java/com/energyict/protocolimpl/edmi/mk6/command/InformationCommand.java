/*
 * InformationCommand.java
 *
 * Created on 21 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 *
 * @author koen
 */
public class InformationCommand extends AbstractCommand {

    private int registerId;
    private char dataType;
    private char measurementUnit;
    private String description;

    /** Creates a new instance of InformationCommand */
    public InformationCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InformationCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   dataType="+getDataType()+"\n");
        strBuff.append("   measurementUnit="+getMeasurementUnit()+"\n");
        strBuff.append("   description="+getDescription()+"\n");
        return strBuff.toString();
    }

    private final char COMMAND='O'; // 'I'

    protected byte[] prepareBuild() {
        if (COMMAND == 'I') {
            byte[] data = new byte[3];
            data[0] = 'I';
            data[1] = (byte)((getRegisterId()>>8)&0xFF);
            data[2] = (byte)((getRegisterId())&0xFF);
            return data;
        } else {
            byte[] data = new byte[5];
            data[0] = 'O';
            data[1] = (byte)((getRegisterId()>>24)&0xFF);
            data[2] = (byte)((getRegisterId()>>16)&0xFF);
            data[3] = (byte)((getRegisterId()>>8)&0xFF);
            data[4] = (byte)((getRegisterId())&0xFF);
            return data;
        }
    }

    protected void parse(byte[] data) throws CommandResponseException, ProtocolException {
        int offset = 1;

        if (COMMAND != (char)data[0]) {
			throw new CommandResponseException("InformationCommand, request command "+COMMAND+" != response command "+(char)data[0]);
		}

        if (COMMAND == 'I') {
            int tempRegisterId = ProtocolUtils.getInt(data,offset,2);
            if (tempRegisterId != getRegisterId()) {
				throw new CommandResponseException("InformationCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
			}
            setRegisterId(tempRegisterId);
            offset+=2;
        }
        else if (COMMAND == 'O') {
            int tempRegisterId = ProtocolUtils.getInt(data,offset,4);
            if (tempRegisterId != getRegisterId()) {
				throw new CommandResponseException("InformationCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
			}
            setRegisterId(tempRegisterId);
            offset+=4;
        }
        setDataType((char)data[offset++]);
        setMeasurementUnit((char)data[offset++]);
        setDescription(new String(ProtocolUtils.getSubArray(data, offset, data.length-2)));
    }

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public char getDataType() {
        return dataType;
    }

    public void setDataType(char dataType) {
        this.dataType = dataType;
    }

    public char getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(char measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
