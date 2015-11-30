/*
 * GeneralDiagnosticInfo.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures.AbstractRemoteProcedure;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RemoteProcedureCall extends AbstractDataDefinition {

    //int functionName;
    //byte[] parameters;

    private AbstractRemoteProcedure remoteProcedure;

    /** Creates a new instance of GeneralDiagnosticInfo */
    public RemoteProcedureCall(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    protected byte[] prepareBuild() {
        int functionName = remoteProcedure.getFunctionName();
        byte[] parameters =  remoteProcedure.getParameters();

        byte[] data = new byte[parameters.length+2];
        System.arraycopy(parameters,0,data, 2, parameters.length);
        data[1] = (byte)functionName;
        data[0] = (byte)(functionName>>8);
        return data;
    }

    protected int getVariableName() {
        return 0x0031; // 49 DLMS_REMOTE_PROCEDURE_CALL
    }

    protected void parse(byte[] data) throws IOException {

    }

    public AbstractRemoteProcedure getRemoteProcedure() {
        return remoteProcedure;
    }

    public void setRemoteProcedure(AbstractRemoteProcedure remoteProcedure) {
        this.remoteProcedure = remoteProcedure;
    }
}
