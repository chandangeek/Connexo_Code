/*
 * TerminateLoadCommand.java
 *
 * Created on 4 december 2006, 15:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class TerminateLoadCommand extends AbstractConfirmedServiceCommand {
    
    
    /** Creates a new instance of TerminateLoadCommand */
    public TerminateLoadCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    
    protected int getCommandId() {
        return 0x11;
        
    }
    
    protected byte[] prepareBuild() {
        
        return null;
        
    }

    
}
