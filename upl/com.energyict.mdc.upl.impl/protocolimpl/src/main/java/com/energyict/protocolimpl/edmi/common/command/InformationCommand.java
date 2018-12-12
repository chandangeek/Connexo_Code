package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 *
 * @author koen
 */
public class InformationCommand extends AbstractCommand {

    private static final char INFORMATION_COMMAND = 'I';
    private static final char EXTENDED_INFORMATION_COMMAND = 'O';

    private int registerId;
    private char dataType;
    private char measurementUnit;

    private final char command;
    
    /** Creates a new instance of InformationCommand */
    public InformationCommand(CommandFactory commandFactory) {
        super(commandFactory);
        command = commandFactory.getProtocol().useExtendedCommand() ? EXTENDED_INFORMATION_COMMAND : INFORMATION_COMMAND;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("InformationCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   dataType="+getDataType()+"\n");
        strBuff.append("   measurementUnit="+getMeasurementUnit()+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() {
        if (command == INFORMATION_COMMAND) {
            byte[] data = new byte[3];
            data[0] = INFORMATION_COMMAND;
            data[1] = (byte)((getRegisterId()>>8)&0xFF);
            data[2] = (byte)((getRegisterId())&0xFF);
            return data;
        } else {
            byte[] data = new byte[5];
            data[0] = EXTENDED_INFORMATION_COMMAND;
            data[1] = (byte)((getRegisterId()>>24)&0xFF);
            data[2] = (byte)((getRegisterId()>>16)&0xFF);
            data[3] = (byte)((getRegisterId()>>8)&0xFF);
            data[4] = (byte)((getRegisterId())&0xFF);
            return data;
        }
    }
    
    protected void parse(byte[] data) throws CommandResponseException {
        int offset = 1;
        if (command == INFORMATION_COMMAND) {
            if (data.length < 5) {
                throw new CommandResponseException("Response for Information command should have a minimum length of 5; actual size was " + data.length);
            } else if (command != (char)data[0]) {
    			throw new CommandResponseException("InformationCommand, request command "+ command +" != response command "+(char)data[0]);
    		}

            int tempRegisterId = ProtocolTools.getIntFromBytes(data,offset,2);
            if (tempRegisterId != getRegisterId()) {
				throw new CommandResponseException("InformationCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
			}
            setRegisterId(tempRegisterId);
            offset+=2;
        } else if (command == EXTENDED_INFORMATION_COMMAND) {
            if (data.length < 7) {
                throw new CommandResponseException("Response for Extended information command should have a minimum length of 7; actual size was " + data.length);
            } else if (command != (char) data[0]) {
                throw new CommandResponseException("Extended informationCommand, request command " + command + " != response command " + (char) data[0]);
            }

            int tempRegisterId = ProtocolTools.getIntFromBytes(data,offset,4);
            if (tempRegisterId != getRegisterId()) {
				throw new CommandResponseException("InformationCommand, request regnum "+getRegisterId()+" != response regnum "+tempRegisterId);
			}
            setRegisterId(tempRegisterId);
            offset+=4;
        }
        setDataType((char)data[offset++]);
        setMeasurementUnit((char)data[offset]);
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
}