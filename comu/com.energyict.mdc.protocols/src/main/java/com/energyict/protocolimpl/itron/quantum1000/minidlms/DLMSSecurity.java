/*
 * DLMSSecurity.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DLMSSecurity extends AbstractDataDefinition {

    private String password;

    /** Creates a new instance of DLMSSecurity */
    public DLMSSecurity(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DLMSSecurity:\n");
        strBuff.append("   password="+getPassword()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        byte[] data = new byte[20];
        if (getPassword() != null) {
            byte[] passwordBytes = getPassword().getBytes();
            System.arraycopy(passwordBytes,0, data, 0, passwordBytes.length>data.length?data.length:passwordBytes.length);
        }
        return data;
    }

    protected int getVariableName() {
        return 0x000A; // 10
    }

    protected void parse(byte[] data) throws IOException {

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
