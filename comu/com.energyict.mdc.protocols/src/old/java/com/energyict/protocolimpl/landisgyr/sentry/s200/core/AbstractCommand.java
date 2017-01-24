/*
 * AbstractCommand.java
 *
 * Created on 26 juli 2006, 17:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractCommand {

    private CommandFactory commandFactory;

    abstract protected void parse(byte[] data) throws IOException;

    abstract protected CommandDescriptor getCommandDescriptor();

    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.setCommandFactory(commandFactory);
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    protected byte[] prepareData() throws IOException {
        return new byte[6];
    }

    public void build() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(prepareData());
        baos.write(getCommandFactory().getS200().getInfoTypePassword().getBytes());
        ResponseData responseData = getCommandFactory().getS200().getS200Connection().sendCommand(getCommandDescriptor().getCommand(), baos.toByteArray());
        if (responseData!=null) {

            if (responseData.getStatus().isError()) {

                StringBuffer strBuff = new StringBuffer();
                if (responseData.getStatus().isCommandFormatError())
                    strBuff.append("command format error, ");
                if (responseData.getStatus().isCrcError())
                    strBuff.append("crc error, ");
                if (responseData.getStatus().isFramingError())
                    strBuff.append("framing error, ");
                if (responseData.getStatus().isOverrunError())
                    strBuff.append("overrun error, ");
                if (responseData.getStatus().isPasswordError())
                    strBuff.append("password error, ");
                if (responseData.getStatus().isProgramMalfunction())
                    strBuff.append("program malfunction error, ");
                if (responseData.getStatus().isRamFailure())
                    strBuff.append("ram failure, ");
                if (responseData.getStatus().isRomFailure())
                    strBuff.append("rom failure, ");


                throw new IOException("AbstractCommand, "+getCommandDescriptor()+", "+strBuff.toString());
            }

            parse(responseData.getData());
        }


    }

}
