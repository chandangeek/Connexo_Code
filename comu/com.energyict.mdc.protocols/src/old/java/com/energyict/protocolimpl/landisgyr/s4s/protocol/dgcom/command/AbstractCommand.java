/*
 * AbstractCommand.java
 *
 * Created on 22 mei 2006, 15:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.ResponseData;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractCommand {

    private CommandFactory commandFactory;
    private boolean responseData;
    int size;
    abstract protected byte[] prepareBuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;

    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.setCommandFactory(commandFactory);
        setResponseData(true);
        setSize(0);
    }

    public void invoke() throws IOException {
        byte[] data = prepareBuild();

        if (!(this instanceof UnlockCommand)) {
            if (getCommandFactory().getS4s().getInfoTypeSecurityLevel()>0) {
                getCommandFactory().unlock(getCommandFactory().getS4s().getInfoTypePassword());
            }
            else {
                if ((this instanceof DateCommand) || (this instanceof TimeCommand)) {
                    if (!isResponseData())
                        getCommandFactory().unlock(getCommandFactory().getS4s().getInfoTypePassword());
                }
            }
        }

        ResponseData rd = getCommandFactory().getS4s().getDgcomConnection().sendCommand(data, isResponseData(),size);
        if (isResponseData())
            parse(rd.getData());
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public boolean isResponseData() {
        return responseData;
    }

    public void setResponseData(boolean responseData) {
        this.responseData = responseData;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size=size;
    }
}
