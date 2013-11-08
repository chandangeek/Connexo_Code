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
abstract public class AbstractWriteCommand extends AbstractCommand {
    
    
    abstract protected byte[] prepareBuild();
    abstract protected int getAccessTag(); 
    
    
    
    private int variableAccessSpec; // 8 bit
    private int variableName; // 16 bit
    
    private byte[] data;
    private int dataType;
    
    /** Creates a new instance of ReadCommand */
    public AbstractWriteCommand(CommandFactory commandFactory) {
        super(commandFactory);
        setVariableAccessSpec(0x01);
        setDataType(0x0B);
    }
    
    protected byte[] prepareInvoke() {
        
        byte[] tempData = new byte[5];
        tempData[0] = (byte)getId();
        tempData[1] = (byte)getVariableAccessSpec();
        tempData[2] = (byte)getAccessTag();
        tempData[3] = (byte)(getVariableName()>>8);
        tempData[4] = (byte)(getVariableName());
        
        byte[] prepareBuildData = prepareBuild();
        
        byte[] frame = new byte[tempData.length+(prepareBuildData != null?prepareBuildData.length:0)+1+getData().length];
        
        System.arraycopy(tempData, 0, frame, 0, tempData.length);
        
        if (prepareBuildData != null)
            System.arraycopy(prepareBuildData, 0, frame, tempData.length, prepareBuildData.length);
        
        frame[tempData.length+(prepareBuildData != null?prepareBuildData.length:0)] = (byte)getDataType();
        
        System.arraycopy(getData(), 0, frame, tempData.length+(prepareBuildData != null?prepareBuildData.length:0)+1, getData().length);
        
        return frame;
    }
    
    
    protected int getId() {
        return 0x06;
    }
    
    


    public int getVariableAccessSpec() {
        return variableAccessSpec;
    }

    public void setVariableAccessSpec(int variableAccessSpec) {
        this.variableAccessSpec = variableAccessSpec;
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getDataType() {
        return dataType;
    }

    private void setDataType(int dataType) {
        this.dataType = dataType;
    }
}
