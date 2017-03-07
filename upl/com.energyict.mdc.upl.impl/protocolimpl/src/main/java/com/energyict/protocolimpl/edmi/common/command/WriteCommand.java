package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolUtils;

/**
 * @author koen
 */
public class WriteCommand extends AbstractCommand {

    private static final char EXTENDED_WRITE_COMMAND = 'N';
    private static final char WRITE_COMMAND = 'W';
    private int registerId;
    private byte[] data;

    private final char command;

    /**
     * Creates a new instance of WriteCommand
     */
    public WriteCommand(CommandFactory commandFactory) {
        super(commandFactory);
        command = commandFactory.getProtocol().useExtendedCommand() ? EXTENDED_WRITE_COMMAND : WRITE_COMMAND;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("WriteCommand:\n");
        strBuff.append("   data=" + ProtocolUtils.outputHexString(getData()) + "\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        if (command == EXTENDED_WRITE_COMMAND) {
            byte[] data = new byte[getData().length + 1 + 4];
            data[0] = EXTENDED_WRITE_COMMAND;
            data[1] = (byte) (getRegisterId() >> 24);
            data[2] = (byte) (getRegisterId() >> 16);
            data[3] = (byte) (getRegisterId() >> 8);
            data[4] = (byte) getRegisterId();
            System.arraycopy(getData(), 0, data, 5, getData().length);
            return data;
        } else {
            byte[] data = new byte[getData().length + 1 + 2];
            data[0] = WRITE_COMMAND;
            data[1] = (byte) (getRegisterId() >> 8);
            data[2] = (byte) getRegisterId();
            System.arraycopy(getData(), 0, data, 3, getData().length);
            return data;
        }
    }

    protected void parse(byte[] rawData) {
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }
}