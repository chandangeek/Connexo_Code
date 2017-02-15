/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ReadCommand.java
 *
 * Created on 1 december 2006, 13:43
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
abstract public class AbstractReadCommand extends AbstractCommand {
    
    
    abstract protected byte[] prepareBuild();
    abstract protected int getAccessTag(); 
    
    
    
    private int variableAccessSpec; // 8 bit
    private int variableName; // 16 bit
    
    /** Creates a new instance of ReadCommand */
    public AbstractReadCommand(CommandFactory commandFactory) {
        super(commandFactory);
        setVariableAccessSpec(0x01);
    }
    
    protected byte[] prepareInvoke() {
        
        byte[] tempData = new byte[5];
        tempData[0] = (byte)getId();
        tempData[1] = (byte)getVariableAccessSpec();
        tempData[2] = (byte)getAccessTag();
        tempData[3] = (byte)(getVariableName()>>8);
        tempData[4] = (byte)(getVariableName());
        
        byte[] prepareBuildData = prepareBuild();
        
        byte[] data = new byte[tempData.length+(prepareBuildData != null?prepareBuildData.length:0)];
        
        System.arraycopy(tempData, 0, data, 0, tempData.length);
        if (prepareBuildData != null)
            System.arraycopy(prepareBuildData, 0, data, tempData.length, prepareBuildData.length);
        
        return data;
    }
    
    
    protected int getId() {
        return 0x05;
    }
    
    


    public int getVariableAccessSpec() {
        return variableAccessSpec;
    }

    private void setVariableAccessSpec(int variableAccessSpec) {
        this.variableAccessSpec = variableAccessSpec;
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }
}
