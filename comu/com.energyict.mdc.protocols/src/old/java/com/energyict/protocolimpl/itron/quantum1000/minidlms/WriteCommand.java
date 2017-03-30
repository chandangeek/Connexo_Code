/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Write.java
 *
 * Created on 4 december 2006, 14:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 * @author Koen
 */
public class WriteCommand extends AbstractWriteCommand {
    
    /** Creates a new instance of Write */
    public WriteCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
 
    protected byte[] prepareBuild() {
        return null;
    }
    
    protected int getAccessTag() {
        return 0x00;
    }    
}
