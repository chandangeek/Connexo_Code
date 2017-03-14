package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeParser;
import com.energyict.protocolimpl.edmi.common.core.RegisterUnitParser;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author koen
 */
public class ReadCommand extends AbstractCommand {

    private static final char EXTENDED_READ_COMMAND = 'M';
    private static final char READ_COMMAND = 'R';
    private int registerId;
    private DataType presetDataType;
    private byte[] data;
    private AbstractRegisterType register;
    private Unit unit;

    private final char command;

    /**
     * Creates a new instance of ReadCommand
     */
    public ReadCommand(CommandFactory commandFactory) {
        super(commandFactory);
        command = commandFactory.getProtocol().useExtendedCommand() ? EXTENDED_READ_COMMAND : READ_COMMAND;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("ReadCommand: ");
        strBuff.append("registerId=0x" + Integer.toHexString(getRegisterId()) + ", ");
        strBuff.append("data=" + ProtocolUtils.outputHexString(getData()) + ", ");

        strBuff.append("register=" + getRegister() + ", ");
        strBuff.append("unit=" + getUnit());
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        if (command == EXTENDED_READ_COMMAND) {
            byte[] data = new byte[5];
            data[0] = EXTENDED_READ_COMMAND;
            data[1] = (byte) ((getRegisterId() >> 24) & 0xFF);
            data[2] = (byte) ((getRegisterId() >> 16) & 0xFF);
            data[3] = (byte) ((getRegisterId() >> 8) & 0xFF);
            data[4] = (byte) ((getRegisterId()) & 0xFF);
            return data;
        } else {
            byte[] data = new byte[3];
            data[0] = READ_COMMAND;
            data[1] = (byte) ((getRegisterId() >> 8) & 0xFF);
            data[2] = (byte) ((getRegisterId()) & 0xFF);
            return data;
        }
    }

    protected void parse(byte[] rawData) throws CommandResponseException, ProtocolException {
        if (command == EXTENDED_READ_COMMAND) {
            if (rawData.length < 5) {
                throw new CommandResponseException("Response for readCommand should have a minimum length of 5; actual size was " + rawData.length);
            } else if (command != (char) rawData[0]) {
                throw new CommandResponseException("Extended readCommand, request command " + command + " != response command " + (char) rawData[0]);
            }

            int tempRegisterId = ProtocolTools.getIntFromBytes(rawData, 1, 4);
            if (tempRegisterId != getRegisterId()) {
                throw new CommandResponseException("ReadCommand, request regnum " + getRegisterId() + " != response regnum " + tempRegisterId);
            }
            setRegisterId(tempRegisterId);
            setData(ProtocolUtils.getSubArray(rawData, 5, rawData.length - 1));
        } else if (command == READ_COMMAND) {
            if (rawData.length < 4) {
                throw new CommandResponseException("Response for readCommand should have a minimum length of 4; actual size was " + rawData.length);
            } else if (command != (char) rawData[0]) {
                throw new CommandResponseException("ReadCommand, request command " + command + " != response command " + (char) rawData[0]);
            }

            int tempRegisterId = ProtocolTools.getIntFromBytes(rawData, 1, 2);
            if (tempRegisterId != getRegisterId()) {
                throw new CommandResponseException("ReadCommand, request regnum " + getRegisterId() + " != response regnum " + tempRegisterId);
            }
            setRegisterId(tempRegisterId);
            setData(ProtocolUtils.getSubArray(rawData, 3, rawData.length - 1));
        }

        char type;
        if (getPresetDataType() == null) {
            InformationCommand ic = getCommandFactory().getInformationCommand(getRegisterId());
            type = ic.getDataType();
            setUnit(RegisterUnitParser.parse(ic.getMeasurementUnit()));
        } else {
            type = getPresetDataType().getType();
            setUnit(Unit.getUndefined());
        }

        RegisterTypeParser rtp = new RegisterTypeParser(getCommandFactory().getProtocol().getTimeZone());
        register = rtp.parse2External(type, getData());
    }

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public DataType getPresetDataType() {
        return presetDataType;
    }

    public void setPresetDataType(DataType presetDataType) {
        this.presetDataType = presetDataType;
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