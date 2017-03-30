/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ExitCommand.java
 *
 * Created on 21 maart 2006, 15:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

/**
 *
 * @author koen
 */
public class ExitCommand extends AbstractCommand {
    
    /** Creates a new instance of ExitCommand */
    public ExitCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareBuild() {
       return new byte[]{'X'};
    }
    
    protected void parse(byte[] data) {
        
    }    
}
