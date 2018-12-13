/*
 * HCCommand.java
 *
 * Created on 10 augustus 2005, 16:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

/**
 *
 * @author koen
 */
public class HCCommand extends QuantitiesCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("HC",false,true);
    
    /** Creates a new instance of SCCommand */
    public HCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
 
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    @Override
    protected String getCommandName() {
        return "HC";
    }
}
