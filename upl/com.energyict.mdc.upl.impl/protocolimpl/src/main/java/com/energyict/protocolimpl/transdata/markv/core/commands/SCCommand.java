/*
 * SCCommand.java
 *
 * Created on 10 augustus 2005, 9:58
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
public class SCCommand extends QuantitiesCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("SC",false,true);
    
    /** Creates a new instance of SCCommand */
    public SCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
 
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    @Override
    protected String getCommandName() {
        return "SC";
    }
}
