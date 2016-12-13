/*
 * AbstractCommand.java
 *
 * Created on 9 augustus 2005, 13:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.transdata.markv.core.connection.ResponseFrame;

import java.io.IOException;

/**
 *
 * @author koen
 */
abstract public class AbstractCommand {
    
    private static final int DEBUG=0;
    
    private CommandFactory commandFactory;

    abstract protected CommandIdentification getCommandIdentification();

    /**
     * Getter for the command name, which in fact corresponds to class name
     * (which we cannot use, as classes are obfuscated)
     */
    abstract protected String getCommandName();

    abstract protected void parse(String strData) throws IOException;

    protected void parse(String strData, byte[] xmodemData) throws IOException {
        throw new IOException(Utils.format("Command parsing using xmodem data is not supported for command '{0}'", new Object[]{getCommandName()}));
    }
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.commandFactory=commandFactory;
    }
    
    
    protected void prepareBuild() throws IOException {
        // override to provide extra functionality...
    }
    
    public void build() throws IOException {
        prepareBuild();
        if (getCommandIdentification().isResponse()) {
            ResponseFrame responseFrame = getCommandFactory().getMarkV().getMarkVConnection().sendCommandAndReceive(getCommandIdentification());
            if (getCommandIdentification().isUseBuffer()) {
                responseFrame = getCommandFactory().getMarkV().getMarkVConnection().sendCommandAndReceive(new CommandIdentification("BU",true,false));           
            }
            
            if (DEBUG>=1) System.out.println("KV_DEBUG> getCommandIdentification()="+getCommandIdentification()+", received data="+responseFrame.getStrData());
            if (responseFrame.getXmodemProtocolData() != null) {
                parse(responseFrame.getStrData(), responseFrame.getXmodemProtocolData());
            } else {
                parse(responseFrame.getStrData());
            }
        }
        else getCommandFactory().getMarkV().getMarkVConnection().sendCommand(getCommandIdentification());
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }
    
}
