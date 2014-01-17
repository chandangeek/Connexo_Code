/*
 * XID.java
 *
 * Created on 19 juni 2006, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class XID extends AbstractSPDU {

    private int slavePassword;

    /** Creates a new instance of XID */
    public XID(SPDUFactory sPDUFactory) {
        super(sPDUFactory);
    }


    protected byte[] prepareBuild() throws IOException {
        byte[] data = new byte[5];
        data[0] = SPDU_XID;
        data[1] = 0; // master ID = 0
        data[2] = 0; // master ID = 0
        data[3] = (byte)(getSlavePassword());
        data[4] = (byte)(getSlavePassword()>>8);
        return data;
    }

    protected void parse(byte[] data) throws IOException {
        int pa = ProtocolUtils.getIntLE(data,2,2);
        if (pa != slavePassword){
            throw new IOException("Password verification failed! config="+slavePassword+", meter="+pa);
        }

   }

    public int getSlavePassword() {
        return slavePassword;
    }

    public void setSlavePassword(int slavePassword) {
        this.slavePassword = slavePassword;
    }

} // public class XID extends AabstractSPDU
