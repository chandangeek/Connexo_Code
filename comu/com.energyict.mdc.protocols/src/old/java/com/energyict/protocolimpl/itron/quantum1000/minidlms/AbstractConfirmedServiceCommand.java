/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractConfirmedServiceCommand.java
 *
 * Created on 4 december 2006, 15:54
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
abstract public class AbstractConfirmedServiceCommand extends AbstractCommand {
    
    abstract protected int getCommandId();
    abstract protected byte[] prepareBuild();
    
    /** Creates a new instance of AbstractConfirmedServiceCommand */
    public AbstractConfirmedServiceCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareInvoke() {
        
        byte[] tempData = new byte[2];
        tempData[0] = (byte)getId();
        tempData[1] = (byte)getCommandId();
        
        byte[] prepareBuildData = prepareBuild();
        
        byte[] data = new byte[tempData.length+(prepareBuildData != null?prepareBuildData.length:0)];
        
        System.arraycopy(tempData, 0, data, 0, tempData.length);
        if (prepareBuildData != null)
            System.arraycopy(prepareBuildData, 0, data, tempData.length, prepareBuildData.length);
        
        return data;
    }
    
    protected int getId() {
        return 0x00;
    }
    
        
}
